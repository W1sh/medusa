package com.w1sh.medusa.listeners.playlists;

import com.w1sh.medusa.data.Playlist;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.playlists.DeletePlaylistEvent;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.services.PlaylistService;
import discord4j.core.object.entity.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

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
        return Mono.justOrEmpty(event.getMember())
                .map(member -> member.getId().asLong())
                .flatMapMany(playlistService::findAllByUserId)
                .collectList()
                .zipWith(Mono.justOrEmpty(event.getMessage().getContent())
                        .map(content -> Integer.parseInt(content.split(" ")[1])))
                .map(tuple -> playlistService.removeIndex(tuple.getT1(), tuple.getT2()))
                .flatMap(playlists -> createSuccessTextMessage(playlists, event))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> logger.error("Failed to delete playlist", throwable)))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<TextMessage> createSuccessTextMessage(List<Playlist> playlists, DeletePlaylistEvent event){
        return event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel, String.format("**%s**, deleted playlist, you now have %d playlists",
                        event.getMember().flatMap(Member::getNickname).orElse(""),
                        playlists.size()), false));
    }

}
