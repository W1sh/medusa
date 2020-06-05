package com.w1sh.medusa.listeners.playlists;

import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.playlists.DeletePlaylistEvent;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.services.PlaylistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class DeletePlaylistListener implements EventListener<DeletePlaylistEvent> {

    private static final Logger logger = LoggerFactory.getLogger(DeletePlaylistListener.class);

    private final PlaylistService playlistService;
    private final ResponseDispatcher responseDispatcher;

    public DeletePlaylistListener(PlaylistService playlistService, ResponseDispatcher responseDispatcher) {
        this.playlistService = playlistService;
        this.responseDispatcher = responseDispatcher;
    }

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
                .onErrorResume(throwable -> Mono.fromRunnable(() -> logger.error("Failed to delete playlist", throwable)))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<TextMessage> createSuccessTextMessage(DeletePlaylistEvent event){
        return event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel, "Playlist has been deleted!", false));
    }
}
