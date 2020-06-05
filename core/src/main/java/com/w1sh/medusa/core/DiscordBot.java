package com.w1sh.medusa.core;

import com.w1sh.medusa.dispatchers.MedusaEventDispatcher;
import com.w1sh.medusa.utils.EventDispatcherInitializer;
import com.w1sh.medusa.utils.Executor;
import discord4j.core.DiscordClient;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.shard.LocalShardCoordinator;
import discord4j.core.shard.ShardingStrategy;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.request.RouteMatcher;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Routes;
import discord4j.store.jdk.JdkStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.retry.Retry;

import javax.annotation.PostConstruct;
import java.time.Duration;

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

        final var client = DiscordClient.builder(token)
                .onClientResponse(ResponseFunction.emptyIfNotFound())
                .onClientResponse(ResponseFunction.retryWhen(RouteMatcher.route(Routes.MESSAGE_CREATE),
                        Retry.onlyIf(ClientException.isRetryContextStatusCode(500))
                                .exponentialBackoffWithJitter(Duration.ofSeconds(2), Duration.ofSeconds(10))))
                .onClientResponse(ResponseFunction.retryOnceOnErrorStatus(500))
                .build();

        final var gateway = client.gateway()
                .setSharding(ShardingStrategy.recommended())
                .setShardCoordinator(LocalShardCoordinator.create())
                .setAwaitConnections(true)
                .setStoreService(new JdkStoreService())
                .setEventDispatcher(medusaEventDispatcher)
                .setInitialStatus(shardInfo -> Presence.online(Activity.watching("you turn to stone")))
                .login()
                .block();

        assert gateway != null;

        eventDispatcherInitializer.setupDispatcher(gateway);
        eventDispatcherInitializer.registerListeners();
        eventDispatcherInitializer.registerEvents();

        executor.startPointDistribution(gateway);

        gateway.onDisconnect().block();
    }
}
