package com.w1sh.medusa.listeners;

import com.w1sh.medusa.services.UserService;
import com.w1sh.medusa.services.WarningService;
import com.w1sh.medusa.utils.Reactive;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public final class MemberLeaveEventListener implements DiscordEventListener<MemberLeaveEvent> {

    private final UserService userService;
    private final WarningService warningService;

    @Override
    public Mono<Void> execute(MemberLeaveEvent event) {
        final var guildId = event.getGuildId().asString();
        final var userId = event.getUser().getId().asString();

        log.info("User with id <{} was kicked or left guild with id <{}>. Deleting all data associated with user on the guild..", userId, guildId);

        final Publisher<?> userPublisher = userService.deleteByUserIdAndGuildId(userId, guildId)
                .transform(Reactive.ifElse(
                        bool -> Mono.fromRunnable(() -> log.info("User with id <{}> left guild with id <{}>, all data associated was deleted", userId, guildId)),
                        bool -> Mono.fromRunnable(() -> log.warn("Could not delete data associated with user with id <{}> of guild with id <{}>", userId, guildId))));

        final Publisher<?> warningsPublisher = warningService.deleteByUserIdAndGuildId(userId, guildId)
                .transform(Reactive.ifElse(
                        bool -> Mono.fromRunnable(() -> log.info("Deleted all warnings from user with id <{}> in guild with id <{}>", userId, guildId)),
                        bool -> Mono.fromRunnable(() -> log.warn("Warnings from user with id <{}> in guild with id <{}> could not be deleted", userId, guildId))));

        return Mono.when(userPublisher, warningsPublisher)
                .doAfterTerminate(() -> log.info("Data deletion process for user with id <{}> on guild with <{}> has concluded", userId, guildId));
    }
}
