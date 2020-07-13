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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.retry.Retry;

import javax.annotation.PostConstruct;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiscordBot {

    private final EventDispatcherInitializer eventDispatcherInitializer;
    private final MedusaEventDispatcher medusaEventDispatcher;
    private final Executor executor;

    @Value("${discord.token}")
    private String token;

    @PostConstruct
    public void init(){
        log.info("Setting up client...");

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
