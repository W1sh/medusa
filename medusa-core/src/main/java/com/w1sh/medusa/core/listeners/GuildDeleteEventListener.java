package com.w1sh.medusa.core.listeners;

import com.w1sh.medusa.listeners.DiscordEventListener;
import com.w1sh.medusa.services.UserService;
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
public final class GuildDeleteEventListener implements DiscordEventListener<GuildDeleteEvent> {

    private final UserService userService;

    @Override
    public Mono<Void> execute(GuildDeleteEvent event) {
        if (event.isUnavailable()) return Mono.empty();
        final var guildId = event.getGuildId().asString();

        log.info("Bot was kicked or left guild with id <{}>. Deleting all data associated with guild..", guildId);

        final Publisher<?> userPublisher = userService.deleteByGuildId(guildId)
                .transform(Reactive.ifElse(
                        bool -> Mono.fromRunnable(() -> log.info("Users from guild with id <{}> have been deleted", guildId)),
                        bool -> Mono.fromRunnable(() -> log.warn("Users from guild with id <{}> could not be deleted", guildId))));

        return Mono.when(userPublisher)
                .doAfterTerminate(() -> log.info("Data deletion process for guild with id <{}> has concluded", guildId));
    }
}
