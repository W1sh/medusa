package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.PurgeEvent;
import com.w1sh.medusa.listeners.CustomEventListener;
import com.w1sh.medusa.services.PlaylistService;
import com.w1sh.medusa.services.UserService;
import com.w1sh.medusa.services.WarningService;
import com.w1sh.medusa.utils.Reactive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public final class PurgeEventListener implements CustomEventListener<PurgeEvent> {

    private final UserService userService;
    private final WarningService warningService;
    private final PlaylistService playlistService;

    @Override
    public Mono<Void> execute(PurgeEvent event) {
        if (event.getUserId().isEmpty()) return Mono.empty();

        log.info("Deleting all data associated with user with id <{}>", event.getUserId());

        final Publisher<?> userPublisher = userService.deleteByUserId(event.getUserId())
                .transform(Reactive.ifElse(
                        bool -> Mono.fromRunnable(() -> log.info("Deleted data from user with id <{}>", event.getUserId())),
                        bool -> Mono.fromRunnable(() -> log.warn("Could not delete data associated with user with id <{}>", event.getUserId()))));

        final Publisher<?> warningsPublisher = warningService.deleteByUserId(event.getUserId())
                .transform(Reactive.ifElse(
                        bool -> Mono.fromRunnable(() -> log.info("Deleted all warnings from user with id <{}>", event.getUserId())),
                        bool -> Mono.fromRunnable(() -> log.warn("Warnings from user with id <{}> could not be deleted", event.getUserId()))));

        final Publisher<?> playlistsPublisher = playlistService.deleteByUserId(event.getUserId())
                .transform(Reactive.ifElse(
                        bool -> Mono.fromRunnable(() -> log.info("Deleted playlists from user with id <{}>", event.getUserId())),
                        bool -> Mono.fromRunnable(() -> log.warn("Could not delete playlists associated with user with id <{}>", event.getUserId()))));

        return Mono.when(userPublisher, warningsPublisher, playlistsPublisher)
                .doAfterTerminate(() -> log.info("Data deletion process for user with id <{}> has concluded", event.getUserId()));
    }
}
