package com.w1sh.medusa.core;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.validators.Validator;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.User;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.bool.BooleanUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public final class MedusaReactiveEventAdapter extends ReactiveEventAdapter {

    private static final Logger log = LoggerFactory.getLogger(MedusaReactiveEventAdapter.class);

    private final EventFactory eventFactory;
    private final CustomEventPublisher customEventPublisher;
    private final DiscordEventPublisher discordEventPublisher;
    private final List<Validator<Event>> eventValidators;
    private final List<Validator<MessageCreateEvent>> messageValidators;

    public MedusaReactiveEventAdapter(EventFactory eventFactory, CustomEventPublisher customEventPublisher,
                                      DiscordEventPublisher discordEventPublisher, List<Validator<Event>> eventValidators,
                                      List<Validator<MessageCreateEvent>> messageValidators) {
        this.eventFactory = eventFactory;
        this.customEventPublisher = customEventPublisher;
        this.discordEventPublisher = discordEventPublisher;
        this.eventValidators = eventValidators;
        this.messageValidators = messageValidators;
    }

    @NonNull
    @Override
    public Publisher<?> onReady(ReadyEvent event) {
        return Mono.justOrEmpty(event.getGuilds().size())
                .flatMap(size -> event.getClient().getEventDispatcher()
                        .on(GuildCreateEvent.class)
                        .take(size)
                        .last())
                .doOnNext(ev -> log.info("All guilds have been received, the client is fully connected"))
                .flatMap(ev -> ev.getClient().getGuilds().count())
                .doOnNext(guilds -> log.info("Currently serving {} guilds", guilds))
                .then();
    }

    @NonNull
    @Override
    public Publisher<?> onMessageCreate(@NonNull MessageCreateEvent event) {
        return Mono.justOrEmpty(event)
                .filter(e -> e.getClass().equals(MessageCreateEvent.class) && e.getMember().map(user -> !user.isBot()).orElse(false))
                .filterWhen(ev -> Flux.fromIterable(messageValidators)
                        .flatMap(validator -> validator.validate(ev))
                        .all(Boolean.TRUE::equals)
                        .defaultIfEmpty(true))
                .flatMap(e -> Mono.justOrEmpty(eventFactory.extractEvents(e)))
                .filterWhen(ev -> Flux.fromIterable(eventValidators)
                        .filter(validator -> Event.class.isAssignableFrom(ev.getClass()))
                        .flatMap(validator -> validator.validate(ev))
                        .all(Boolean.TRUE::equals)
                        .defaultIfEmpty(true))
                .flatMap(customEventPublisher::publish);
    }

    @NonNull
    @Override
    public Publisher<?> onReactionAdd(@NonNull ReactionAddEvent event) {
        return Mono.justOrEmpty(event)
                .filterWhen(e -> BooleanUtils.not(e.getUser().map(User::isBot)))
                .flatMap(discordEventPublisher::publish);
    }

    @NonNull
    @Override
    public Publisher<?> onDisconnect(DisconnectEvent event) {
        Throwable cause = event.getCause().orElse(null);
        log.error("Disconnected from gateway with status {}", event.getStatus(), cause);
        return super.onDisconnect(event);
    }
}
