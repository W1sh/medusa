package com.w1sh.medusa.listeners.playlists;

import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.events.PlaylistEvent;
import com.w1sh.medusa.services.PlaylistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public final class PlaylistDeleteAction implements Function<PlaylistEvent, Mono<? extends Response>> {

    private static final String DELETE_ALL_PARAMETER = "all";

    private final PlaylistService playlistService;

    @Override
    public Mono<? extends Response> apply(PlaylistEvent event) {
        if (DELETE_ALL_PARAMETER.equalsIgnoreCase(event.getArguments().get(1))) return deleteAll(event);

        final Integer index = Integer.parseInt(event.getArguments().get(1));

        return playlistService.deleteIndex(event.getUserId(), index)
                .flatMap(ignored -> createDeleteTextMessage(event))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> log.error("Failed to delete playlist", throwable)));
    }

    private Mono<? extends Response> deleteAll(PlaylistEvent event) {
        return playlistService.deleteByUserId(event.getUserId())
                .flatMap(ignored -> createDeleteAllTextMessage(event))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> log.error("Failed to delete all playlists of user with id <{}>",
                        event.getGuildId(), throwable)));
    }

    private Mono<TextMessage> createDeleteTextMessage(PlaylistEvent event){
        return event.getChannel().map(channel -> new TextMessage(channel, "Playlist has been deleted!", false));
    }

    private Mono<TextMessage> createDeleteAllTextMessage(PlaylistEvent event){
        return event.getChannel().map(channel -> new TextMessage(channel, "All your playlists have been deleted!", false));
    }
}
