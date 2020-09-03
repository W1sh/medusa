package com.w1sh.medusa.listeners;

import com.w1sh.medusa.services.ChannelService;
import com.w1sh.medusa.services.UserService;
import com.w1sh.medusa.services.WarningService;
import com.w1sh.medusa.utils.Reactive;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public final class GuildDeleteEventListener implements EventListener<GuildDeleteEvent> {

    private final UserService userService;
    private final WarningService warningService;
    private final ChannelService channelService;

    @Override
    public Mono<Void> execute(GuildDeleteEvent event) {
        if (event.isUnavailable()) return Mono.empty();
        final var guildId = event.getGuildId().asString();

        log.info("Bot was kicked or left guild with id <{}>. Deleting all data associated with guild..", guildId);

        final Publisher<?> userPublisher = userService.deleteByGuildId(guildId)
                .transform(Reactive.ifElse(
                        bool -> Mono.fromRunnable(() -> log.info("Users from guild with id <{}> have been deleted", guildId)),
                        bool -> Mono.fromRunnable(() -> log.warn("Users from guild with id <{}> could not be deleted", guildId))));

        final Publisher<?> channelPublisher = channelService.deleteByGuildId(guildId)
                .transform(Reactive.ifElse(
                        bool -> Mono.fromRunnable(() -> log.info("Text channels from guild with id <{}> have been deleted", guildId)),
                        bool -> Mono.fromRunnable(() -> log.warn("Text channels from guild with id <{}> could not be deleted", guildId))));

        final Publisher<?> warningsPublisher = warningService.deleteByGuildId(guildId)
                .transform(Reactive.ifElse(
                        bool -> Mono.fromRunnable(() -> log.info("Deleted all warnings from guild with id <{}>", guildId)),
                        bool -> Mono.fromRunnable(() -> log.warn("Warnings from guild with id <{}> could not be deleted", guildId))));

        return Mono.when(userPublisher, channelPublisher, warningsPublisher)
                .doAfterTerminate(() -> log.info("Data deletion process for guild with id <{}> has concluded", guildId));
    }
}
