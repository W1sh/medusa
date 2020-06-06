package com.w1sh.medusa.listeners.playlists;

import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.playlists.DeletePlaylistEvent;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.services.PlaylistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public final class DeletePlaylistListener implements EventListener<DeletePlaylistEvent> {

    private final PlaylistService playlistService;
    private final ResponseDispatcher responseDispatcher;

    @Override
    public Class<DeletePlaylistEvent> getEventType() {
        return DeletePlaylistEvent.class;
    }

    @Override
    public Mono<Void> execute(DeletePlaylistEvent event) {
        Integer index = Integer.parseInt(event.getArguments().get(0));
        String userId = event.getMember().map(member -> member.getId().asString()).orElse("");

        return playlistService.deleteIndex(userId, index)
                .flatMap(ignored -> createSuccessTextMessage(event))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> log.error("Failed to delete playlist", throwable)))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<TextMessage> createSuccessTextMessage(DeletePlaylistEvent event){
        return event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel, "Playlist has been deleted!", false));
    }
}
