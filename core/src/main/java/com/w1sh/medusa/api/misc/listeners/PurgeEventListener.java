package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.PingEvent;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.services.PlaylistService;
import com.w1sh.medusa.services.UserService;
import com.w1sh.medusa.services.WarningService;
import com.w1sh.medusa.utils.Reactive;
import discord4j.rest.util.Permission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public final class PurgeEventListener implements EventListener<PingEvent> {

    private final UserService userService;
    private final WarningService warningService;
    private final PlaylistService playlistService;

    @Override
    public Mono<Void> execute(PingEvent event) {
        final var userId = event.getMessage().getAuthor().map(user -> user.getId().asString()).orElse("");
        if (userId.isEmpty()) return Mono.empty();

        log.info("Deleting all data associated with user with id <{}>", userId);

        final Publisher<?> userPublisher = userService.deleteByUserId(userId)
                .transform(Reactive.ifElse(
                        bool -> Mono.fromRunnable(() -> log.info("Deleted data from user with id <{}>", userId)),
                        bool -> Mono.fromRunnable(() -> log.warn("Could not delete data associated with user with id <{}>", userId))));

        final Publisher<?> warningsPublisher = warningService.deleteByUserId(userId)
                .transform(Reactive.ifElse(
                        bool -> Mono.fromRunnable(() -> log.info("Deleted all warnings from user with id <{}>", userId)),
                        bool -> Mono.fromRunnable(() -> log.warn("Warnings from user with id <{}> could not be deleted", userId))));

        final Publisher<?> playlistsPublisher = playlistService.deleteByUserId(userId)
                .transform(Reactive.ifElse(
                        bool -> Mono.fromRunnable(() -> log.info("Deleted playlists from user with id <{}>", userId)),
                        bool -> Mono.fromRunnable(() -> log.warn("Could not delete playlists associated with user with id <{}>", userId))));

        return Mono.when(userPublisher, warningsPublisher, playlistsPublisher)
                .doAfterTerminate(() -> log.info("Data deletion process for user with id <{}> has concluded", userId));
    }
}
