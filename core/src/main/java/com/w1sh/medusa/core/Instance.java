package com.w1sh.medusa.core;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.validators.Validator;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.channel.TextChannelCreateEvent;
import discord4j.core.event.domain.channel.TextChannelDeleteEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.shard.LocalShardCoordinator;
import discord4j.core.shard.ShardingStrategy;
import discord4j.discordjson.json.gateway.GuildMemberRemove;
import discord4j.gateway.intent.Intent;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.request.RouteMatcher;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Routes;
import discord4j.store.jdk.JdkStoreService;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;

import javax.annotation.PostConstruct;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public final class Instance {

    private final EventFactory eventFactory;
    private final EventPublisher eventPublisher;
    private final List<Validator> validators;
    private final Set<Class<? extends Event>> events;
    private final Executor executor;

    @Value("${discord.token}")
    private String token;

    public Instance(EventFactory eventFactory, EventPublisher eventPublisher, List<Validator> validators,
                    Executor executor, Reflections reflections) {
        this.eventFactory = eventFactory;
        this.eventPublisher = eventPublisher;
        this.validators = validators;
        this.executor = executor;
        this.events = reflections.getSubTypesOf(Event.class)
                .stream()
                .filter(event -> !Modifier.isAbstract(event.getModifiers()))
                .collect(Collectors.toSet());
    }

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
                //.setEnabledIntents(IntentSet.of(GUILD_MEMBERS, GUILD_MESSAGES, GUILD_VOICE_STATES))
                .setSharding(ShardingStrategy.recommended())
                .setShardCoordinator(LocalShardCoordinator.create())
                .setAwaitConnections(true)
                .setStoreService(new JdkStoreService())
                .setEventDispatcher(EventDispatcher.buffering())
                .setInitialStatus(shardInfo -> Presence.online(Activity.watching("you turn to stone")))
                .login()
                .block();

        assert gateway != null;

        initDispatcher(gateway);

        registerEvents();

        executor.startPointDistribution(gateway);

        log.info("Client setup completed");
    }

    private void initDispatcher(GatewayDiscordClient gateway) {
        final Publisher<?> onReady = gateway.on(ReadyEvent.class, eventPublisher::publishEvent);

        final Publisher<?> onMemberLeave = gateway.on(MemberLeaveEvent.class, eventPublisher::publishEvent);

        final Publisher<?> onTextChannelCreate = gateway.on(TextChannelCreateEvent.class, eventPublisher::publishEvent);

        final Publisher<?> onTextChannelDelete = gateway.on(TextChannelDeleteEvent.class, eventPublisher::publishEvent);

        final Publisher<?> onMessageUpdate = gateway.on(MessageUpdateEvent.class, eventPublisher::publishEvent);

        final Publisher<?> onMessageCreate = gateway.on(MessageCreateEvent.class)
                .filter(event -> event.getClass().equals(MessageCreateEvent.class) && event.getMember().map(user -> !user.isBot()).orElse(false))
                .flatMap(event -> Mono.justOrEmpty(eventFactory.extractEvents(event)))
                .filterWhen(ev -> Flux.fromIterable(validators)
                        .filter(validator -> Event.class.isAssignableFrom(ev.getClass()))
                        .flatMap(validator -> validator.validate((Event) ev))
                        .all(bool -> true)
                        .defaultIfEmpty(true))
                .flatMap(eventPublisher::publishEvent);

        final Publisher<?> onDisconnect = gateway.onDisconnect()
                .doOnTerminate(() -> log.info("Client disconnected"));

        Mono.when(onReady, onMemberLeave, onTextChannelCreate, onTextChannelDelete, onMessageUpdate, onMessageCreate, onDisconnect)
                .subscribe(null, t -> log.error("An unknown error occurred", t));
    }

    private void registerEvents() {
        for (Class<? extends Event> clazz : events) {
            eventFactory.registerEvent(clazz);
            log.info("Registering new event of type <{}>", clazz.getSimpleName());
        }
        log.info("Found and registered {} events", events.size());
    }
}
