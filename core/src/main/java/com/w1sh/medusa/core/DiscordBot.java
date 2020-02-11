package com.w1sh.medusa.core;

import com.w1sh.medusa.data.events.EventFactory;
import com.w1sh.medusa.dispatchers.MedusaEventDispatcher;
import com.w1sh.medusa.utils.EventDispatcherInitializer;
import com.w1sh.medusa.utils.Executor;
import discord4j.core.DiscordClient;
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

    private final EventDispatcherInitializer eventDispatcherInitializer;
    private final MedusaEventDispatcher medusaEventDispatcher;
    private final Executor executor;

    @Value("${discord.token}")
    private String token;

    public DiscordBot(EventDispatcherInitializer eventDispatcherInitializer, MedusaEventDispatcher medusaEventDispatcher,
                      Executor executor) {
        this.eventDispatcherInitializer = eventDispatcherInitializer;
        this.medusaEventDispatcher = medusaEventDispatcher;
        this.executor = executor;
    }

    @PostConstruct
    public void init(){
        logger.info("Setting up client...");

        var client = DiscordClient.create(token);
        var gateway = client.gateway()
                .setSharding(ShardingStrategy.recommended())
                .setShardCoordinator(new LocalShardCoordinator())
                .setAwaitConnections(true)
                .setStoreService(new JdkStoreService())
                .setEventDispatcher(medusaEventDispatcher)
                .setInitialPresence(shard -> Presence.online(Activity.watching(String.format("Cringe 2 | %shelp", EventFactory.getPrefix()))))
                .connect()
                .block();

        assert gateway != null;

        eventDispatcherInitializer.setupDispatcher(gateway);
        eventDispatcherInitializer.registerListeners();
        eventDispatcherInitializer.registerEvents();

        executor.startPointDistribution(gateway);

        gateway.onDisconnect().block();
    }
}
