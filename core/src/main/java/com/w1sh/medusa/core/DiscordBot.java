package com.w1sh.medusa.core;

import com.w1sh.medusa.data.events.EventFactory;
import com.w1sh.medusa.dispatchers.MedusaEventDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.utils.ApplicationContextUtils;
import com.w1sh.medusa.utils.Executor;
import com.w1sh.medusa.validators.ArgumentValidator;
import com.w1sh.medusa.validators.PermissionsValidator;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.shard.LocalShardCoordinator;
import discord4j.core.shard.ShardingStrategy;
import discord4j.store.jdk.JdkStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DiscordBot {

    private static final Logger logger = LoggerFactory.getLogger(DiscordBot.class);

    private final ApplicationContextUtils applicationContextUtils;
    private final MedusaEventDispatcher medusaEventDispatcher;
    private final Executor executor;

    private final ArgumentValidator argumentValidator;
    private final PermissionsValidator permissionsValidator;

    @Value("${discord.token}")
    private String token;

    public DiscordBot(ApplicationContextUtils applicationContextUtils, MedusaEventDispatcher medusaEventDispatcher, Executor executor,
                      ArgumentValidator argumentValidator, PermissionsValidator permissionsValidator) {
        this.applicationContextUtils = applicationContextUtils;
        this.medusaEventDispatcher = medusaEventDispatcher;
        this.executor = executor;

        this.argumentValidator = argumentValidator;
        this.permissionsValidator = permissionsValidator;
    }

    @PostConstruct
    public void init(){
        logger.info("Setting up client...");

        var client = DiscordClient.create(token);
        var gateway = client.gateway()
                .setSharding(ShardingStrategy.recommended())
                .setShardCoordinator(new LocalShardCoordinator())
                .setEventDispatcher(EventDispatcher.buffering())
                .setAwaitConnections(true)
                .setStoreService(new JdkStoreService())
                .setEventDispatcher(medusaEventDispatcher)
                .setInitialPresence(shard -> Presence.online(Activity.watching(String.format("Cringe 2 | %shelp", EventFactory.getPrefix()))))
                .connect()
                .block();

        assert gateway != null;

        var listeners = applicationContextUtils.findAllByType(EventListener.class);
        listeners.forEach(medusaEventDispatcher::registerListener);
        logger.info("Found and registered {} event listeners", listeners.size());

        setupEventDispatcher(gateway);

        executor.startPointDistribution(gateway);

        gateway.onDisconnect().block();
    }

    private void setupEventDispatcher(GatewayDiscordClient gateway){
        gateway.on(MessageCreateEvent.class)
                .filter(event -> event.getClass().equals(MessageCreateEvent.class))
                .filter(event -> event.getMember().isPresent() && event.getMember().map(user -> !user.isBot()).orElse(false))
                .map(EventFactory::extractEvents)
                .filterWhen(argumentValidator::validate)
                .filterWhen(permissionsValidator::validate)
                .doOnSubscribe(ev -> logger.info("Received new event of type <{}>", ev.getClass().getSimpleName()))
                .subscribe(medusaEventDispatcher::publish);
    }

}
