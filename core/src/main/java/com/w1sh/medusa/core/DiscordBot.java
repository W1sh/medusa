package com.w1sh.medusa.core;

import com.w1sh.medusa.data.events.EventFactory;
import com.w1sh.medusa.dispatchers.MedusaEventDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.service.UserService;
import com.w1sh.medusa.validators.ArgumentValidator;
import com.w1sh.medusa.validators.PermissionsValidator;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.presence.Status;
import discord4j.core.shard.LocalShardCoordinator;
import discord4j.core.shard.ShardingStrategy;
import discord4j.store.jdk.JdkStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Component
public class DiscordBot {

    private static final Logger logger = LoggerFactory.getLogger(DiscordBot.class);

    private final ApplicationContext applicationContext;
    private final MedusaEventDispatcher medusaEventDispatcher;
    private final ArgumentValidator argumentValidator;
    private final PermissionsValidator permissionsValidator;
    private final UserService userService;

    @Value("${points.reward.delay}")
    private String rewardDelay;

    @Value("${points.reward.period}")
    private String rewardPeriod;

    @Value("${discord.token}")
    private String token;

    public DiscordBot(ApplicationContext applicationContext, MedusaEventDispatcher medusaEventDispatcher, ArgumentValidator argumentValidator,
                      PermissionsValidator permissionsValidator, UserService userService) {
        this.applicationContext = applicationContext;

        this.medusaEventDispatcher = medusaEventDispatcher;
        this.argumentValidator = argumentValidator;
        this.permissionsValidator = permissionsValidator;
        this.userService = userService;
    }

    @PostConstruct
    public void init(){
        logger.info("Setting up client...");
        DiscordClient client = DiscordClient.create(token);

        GatewayDiscordClient gateway = client.gateway()
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

        findAllEventListeners();
        setupCommandEventDispatcher(gateway);

        Schedulers.boundedElastic().schedulePeriodically(() -> schedulePointDistribution(gateway),
                Integer.parseInt(rewardDelay),
                Integer.parseInt(rewardPeriod),
                TimeUnit.MINUTES);

        gateway.onDisconnect().block();
    }

    private void setupCommandEventDispatcher(GatewayDiscordClient gateway){
        gateway.on(MessageCreateEvent.class)
                .filter(event -> event.getClass().equals(MessageCreateEvent.class))
                .filter(event -> event.getMember().isPresent() && event.getMember().map(user -> !user.isBot()).orElse(false))
                .map(EventFactory::extractEvents)
                .filterWhen(argumentValidator::validate)
                .filterWhen(permissionsValidator::validate)
                .doOnSubscribe(ev -> logger.info("Received new event of type <{}>", ev.getClass().getSimpleName()))
                .subscribe(medusaEventDispatcher::publish);
    }

    private void findAllEventListeners(){
        Collection<EventListener> eventListeners = applicationContext.getBeansOfType(EventListener.class).values();
        eventListeners.forEach(medusaEventDispatcher::registerListener);
    }

    public void schedulePointDistribution(GatewayDiscordClient gateway) {
        logger.info("Sending points to all active members");
        gateway.getGuilds()
                .flatMap(Guild::getMembers)
                .filterWhen(this::isEligibleForRewards)
                .flatMap(userService::distributePoints)
                .subscribe();
    }

    private Mono<Boolean> isEligibleForRewards(Member member) {
        return Mono.just(member)
                .filter(m -> !m.isBot())
                .flatMap(Member::getPresence)
                .map(Presence::getStatus)
                .filter(status -> status.equals(Status.ONLINE) || status.equals(Status.IDLE)
                        || status.equals(Status.DO_NOT_DISTURB))
                .hasElement();
    }
}
