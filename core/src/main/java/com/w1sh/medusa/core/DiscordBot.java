package com.w1sh.medusa.core;

import com.w1sh.medusa.data.events.EventFactory;
import com.w1sh.medusa.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.listeners.DisconnectListener;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.listeners.ReadyListener;
import com.w1sh.medusa.listeners.VoiceStateUpdateListener;
import com.w1sh.medusa.service.UserService;
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
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Component
public class DiscordBot {

    private static final Logger logger = LoggerFactory.getLogger(DiscordBot.class);

    private final VoiceStateUpdateListener voiceStateUpdateListener;
    private final ReadyListener readyListener;
    private final DisconnectListener disconnectListener;
    private final CommandEventDispatcher commandEventDispatcher;
    private final UserService userService;

    private DiscordClient client;
    private GatewayDiscordClient gateway;

    @Value("${points.reward.delay}")
    private String rewardDelay;

    @Value("${points.reward.period}")
    private String rewardPeriod;

    @Value("${discord.token}")
    private String token;

    public DiscordBot(VoiceStateUpdateListener voiceStateUpdateListener,
                      ReadyListener readyListener, DisconnectListener disconnectListener,
                      CommandEventDispatcher commandEventDispatcher, UserService userService) {
        this.voiceStateUpdateListener = voiceStateUpdateListener;
        this.readyListener = readyListener;
        this.disconnectListener = disconnectListener;
        this.commandEventDispatcher = commandEventDispatcher;
        this.userService = userService;
    }

    @PostConstruct
    public void init(){
        logger.info("Setting up client...");
        client = DiscordClient.create(token);

        gateway = client.gateway()
                .setSharding(ShardingStrategy.recommended())
                .setShardCoordinator(new LocalShardCoordinator())
                .setEventDispatcher(EventDispatcher.buffering())
                .setAwaitConnections(true)
                .setStoreService(new JdkStoreService())
                //.setStoreService(new RedisStoreService(RedisStoreService.defaultClient()))
                .setInitialPresence(shard -> Presence.online(Activity.watching(String.format("Cringe 2 | %shelp", EventFactory.getPrefix()))))
                .connect()
                .block();

        /*setupEventDispatcher(disconnectListener);
        setupEventDispatcher(readyListener);
        setupEventDispatcher(voiceStateUpdateListener);

        setupCommandEventDispatcher();

        Schedulers.boundedElastic().schedulePeriodically(this::schedulePointDistribution,
                Integer.parseInt(rewardDelay),
                Integer.parseInt(rewardPeriod),
                TimeUnit.MINUTES);*/

        assert gateway != null;
        gateway.onDisconnect().block();
    }

    private <T extends Event> void setupEventDispatcher(EventListener<T> eventListener){
        logger.info("Registering new listener to main dispatcher of type <{}>", eventListener.getClass().getSimpleName());
        gateway.on(eventListener.getEventType())
                .flatMap(eventListener::execute)
                .subscribe(null, throwable -> logger.error("Error when consuming events", throwable));
    }

    private void setupCommandEventDispatcher(){
        gateway.on(MessageCreateEvent.class)
                .filter(event -> event.getMember().isPresent() && event.getMember().map(user -> !user.isBot()).orElse(false))
                .subscribe(commandEventDispatcher::publish);
    }

    public void schedulePointDistribution() {
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
