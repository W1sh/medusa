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

    private final PlaylistService playlistService;

    @Override
    public Mono<? extends Response> apply(PlaylistEvent event) {
        Integer index = Integer.parseInt(event.getArguments().get(1));
        String userId = event.getMember().map(member -> member.getId().asString()).orElse("");

        return playlistService.deleteIndex(userId, index)
                .flatMap(ignored -> createSuccessTextMessage(event))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> log.error("Failed to delete playlist", throwable)));
    }

    private Mono<TextMessage> createSuccessTextMessage(PlaylistEvent event){
        return event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel, "Playlist has been deleted!", false));
    }
}
