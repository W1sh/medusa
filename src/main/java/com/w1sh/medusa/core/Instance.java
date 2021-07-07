package com.w1sh.medusa.core;

import discord4j.common.JacksonResources;
import discord4j.core.DiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.shard.LocalShardCoordinator;
import discord4j.core.shard.ShardingStrategy;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.request.RouteMatcher;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Routes;
import discord4j.store.api.service.StoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.retry.Retry;

import java.time.Duration;
import java.time.Instant;

@Component
public final class Instance {

    private static final Instant START_INSTANCE = Instant.now();
    private static final Logger log = LoggerFactory.getLogger(Instance.class);

    private final JacksonResources jacksonResources;
    private final ReactiveEventAdapter reactiveEventAdapter;
    private final StoreService storeService;

    @Value("${medusa.discord.token}")
    private String token;

    public Instance(JacksonResources jacksonResources, ReactiveEventAdapter reactiveEventAdapter,
                    StoreService storeService) {
        this.jacksonResources = jacksonResources;
        this.reactiveEventAdapter = reactiveEventAdapter;
        this.storeService = storeService;
    }

    public void initialize(){
        log.info("Setting up client...");

        DiscordClient.builder(token)
                .onClientResponse(ResponseFunction.emptyIfNotFound())
                .onClientResponse(ResponseFunction.retryWhen(RouteMatcher.route(Routes.MESSAGE_CREATE),
                        Retry.onlyIf(ClientException.isRetryContextStatusCode(500))
                                .exponentialBackoffWithJitter(Duration.ofSeconds(2), Duration.ofSeconds(10))))
                .onClientResponse(ResponseFunction.retryOnceOnErrorStatus(500))
                .setJacksonResources(jacksonResources)
                .build()
                .gateway()
                //.setEnabledIntents(IntentSet.of(GUILD_MEMBERS, GUILD_MESSAGES, GUILD_VOICE_STATES))
                .setSharding(ShardingStrategy.recommended())
                .setShardCoordinator(LocalShardCoordinator.create())
                .setAwaitConnections(true)
                .setStoreService(storeService)
                .setEventDispatcher(EventDispatcher.buffering())
                .setInitialPresence(shardInfo -> Presence.online(Activity.watching("you turn to stone")))
                .login()
                .blockOptional()
                .orElseThrow(RuntimeException::new)
                .on(reactiveEventAdapter)
                .subscribe();

        log.info("Client setup completed");
    }

    public static String getUptime(){
        final long seconds = Duration.between(START_INSTANCE, Instant.now()).getSeconds();
        final long absSeconds = Math.abs(seconds);
        String positive;
        if(absSeconds >= 3600){
            positive = String.format("%d %s, %d %s and %d %s",
                    absSeconds / 3600,
                    absSeconds / 3600 > 1 ? "hours" : "hour",
                    (absSeconds % 3600) / 60,
                    ((absSeconds % 3600) / 60) > 1 ? "minutes" : "minute",
                    absSeconds % 60,
                    absSeconds % 60 > 1 ? "seconds" : "second");
        } else if (absSeconds >= 60){
            positive = String.format("%d %s and %d %s",
                    (absSeconds % 3600) / 60,
                    ((absSeconds % 3600) / 60) > 1 ? "minutes" : "minute",
                    absSeconds % 60,
                    absSeconds % 60 > 1 ? "seconds" : "second");
        } else {
            positive = String.format("%d %s", absSeconds % 60, absSeconds % 60 > 1 ? "seconds" : "second");
        }
        return positive;
    }
}
