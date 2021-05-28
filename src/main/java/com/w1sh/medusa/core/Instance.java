package com.w1sh.medusa.core;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.validators.Validator;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.shard.LocalShardCoordinator;
import discord4j.core.shard.ShardingStrategy;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.request.RouteMatcher;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Routes;
import discord4j.store.jdk.JdkStoreService;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.bool.BooleanUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
public final class Instance {

    public static final Instant START_INSTANCE = Instant.now();

    private final EventFactory eventFactory;
    private final CustomEventPublisher customEventPublisher;
    private final DiscordEventPublisher discordEventPublisher;
    private final List<Validator<Event>> eventValidators;
    private final List<Validator<MessageCreateEvent>> messageValidators;

    @Value("${discord.token}")
    private String token;

    public Instance(EventFactory eventFactory, CustomEventPublisher customEventPublisher, DiscordEventPublisher discordEventPublisher,
                    List<Validator<Event>> eventValidators, List<Validator<MessageCreateEvent>> messageValidators) {
        this.eventFactory = eventFactory;
        this.customEventPublisher = customEventPublisher;
        this.discordEventPublisher = discordEventPublisher;
        this.eventValidators = eventValidators;
        this.messageValidators = messageValidators;
    }

    public void initialize(){
        log.info("Setting up client...");

        final var gateway = DiscordClient.builder(token)
                .onClientResponse(ResponseFunction.emptyIfNotFound())
                .onClientResponse(ResponseFunction.retryWhen(RouteMatcher.route(Routes.MESSAGE_CREATE),
                        Retry.onlyIf(ClientException.isRetryContextStatusCode(500))
                                .exponentialBackoffWithJitter(Duration.ofSeconds(2), Duration.ofSeconds(10))))
                .onClientResponse(ResponseFunction.retryOnceOnErrorStatus(500))
                .build()
                .gateway()
                //.setEnabledIntents(IntentSet.of(GUILD_MEMBERS, GUILD_MESSAGES, GUILD_VOICE_STATES))
                .setSharding(ShardingStrategy.recommended())
                .setShardCoordinator(LocalShardCoordinator.create())
                .setAwaitConnections(true)
                .setStoreService(new JdkStoreService())
                .setEventDispatcher(EventDispatcher.buffering())
                .setInitialStatus(shardInfo -> Presence.online(Activity.watching("you turn to stone")))
                .login()
                .blockOptional()
                .orElseThrow(RuntimeException::new);

        initDispatcher(gateway);

        log.info("Client setup completed");
    }

    private void initDispatcher(GatewayDiscordClient gateway) {
        final Publisher<?> onReady = gateway.on(ReadyEvent.class, discordEventPublisher::publish);

        final Publisher<?> onReactionAdd = gateway.on(ReactionAddEvent.class)
                .filterWhen(event -> BooleanUtils.not(event.getUser().map(User::isBot)))
                .flatMap(discordEventPublisher::publish);

        final Publisher<?> onMessageUpdate = gateway.on(MessageUpdateEvent.class)
                .filterWhen(event -> BooleanUtils.not(event.getMessage()
                        .flatMap(Message::getAuthorAsMember)
                        .map(User::isBot)))
                .flatMap(discordEventPublisher::publish);

        final Publisher<?> onMessageCreate = gateway.on(MessageCreateEvent.class)
                .filter(event -> event.getClass().equals(MessageCreateEvent.class) && event.getMember().map(user -> !user.isBot()).orElse(false))
                .filterWhen(ev -> Flux.fromIterable(messageValidators)
                        .flatMap(validator -> validator.validate(ev))
                        .all(Boolean.TRUE::equals)
                        .defaultIfEmpty(true))
                .flatMap(event -> Mono.justOrEmpty(eventFactory.extractEvents(event)))
                .filterWhen(ev -> Flux.fromIterable(eventValidators)
                        .filter(validator -> Event.class.isAssignableFrom(ev.getClass()))
                        .flatMap(validator -> validator.validate(ev))
                        .all(Boolean.TRUE::equals)
                        .defaultIfEmpty(true))
                .flatMap(customEventPublisher::publish);

        final Publisher<?> onDisconnect = gateway.onDisconnect()
                .doOnTerminate(() -> log.info("Client disconnected"));

        Mono.when(onReady, onReactionAdd, onMessageUpdate, onMessageCreate, onDisconnect)
                .subscribe(null, t -> log.error("An unknown error occurred", t));
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
