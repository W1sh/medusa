package com.w1sh.medusa.listeners.playlists;

import com.w1sh.medusa.AudioConnection;
import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.TrackScheduler;
import com.w1sh.medusa.core.data.TextMessage;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.events.playlists.SavePlaylistEvent;
import com.w1sh.medusa.mongo.entities.Playlist;
import com.w1sh.medusa.mongo.entities.Track;
import com.w1sh.medusa.mongo.services.PlaylistService;
import discord4j.core.object.entity.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class SavePlaylistListener implements EventListener<SavePlaylistEvent> {

    private static final Logger logger = LoggerFactory.getLogger(SavePlaylistListener.class);

    private final PlaylistService playlistService;
    private final ResponseDispatcher responseDispatcher;

    public SavePlaylistListener(ResponseDispatcher responseDispatcher, CommandEventDispatcher eventDispatcher, PlaylistService playlistService) {
        this.responseDispatcher = responseDispatcher;
        this.playlistService = playlistService;
        EventFactory.registerEvent(SavePlaylistEvent.KEYWORD, SavePlaylistEvent.class);
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<SavePlaylistEvent> getEventType() {
        return SavePlaylistEvent.class;
    }

    @Override
    public Mono<Void> execute(SavePlaylistEvent event) {
        Long userId = event.getMember().map(member -> member.getId().asLong()).orElseThrow();

        return Mono.justOrEmpty(event.getGuildId())
                .flatMap(AudioConnectionManager.getInstance()::getAudioConnection)
                .map(AudioConnection::getTrackScheduler)
                .flatMapIterable(TrackScheduler::getFullQueue)
                .map(at -> new Track(at.getInfo().author, at.getInfo().title, at.getInfo().uri, at.getInfo().length))
                .collectList()
                .map(tracks -> new Playlist(userId, tracks))
                .flatMap(playlistService::save)
                .onErrorResume(throwable -> Mono.fromRunnable(() -> logger.error("Failed to save playlist", throwable)))
                .flatMap(playlist -> createSavePlaylistSuccessMessage(event, playlist))
                .switchIfEmpty(createFailedSaveErrorMessage(event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<TextMessage> createFailedSaveErrorMessage(SavePlaylistEvent event){
        return event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel, String.format("**%s**, could not save your playlist, try again later!",
                        event.getMember().flatMap(Member::getNickname).orElse("")), false));
    }

    private Mono<TextMessage> createSavePlaylistSuccessMessage(SavePlaylistEvent event, Playlist playlist){
        return event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel, String.format("**%s**, saved your playlist with %d tracks!",
                        event.getMember().flatMap(Member::getNickname).orElse(""),
                        playlist.getTracks().size()), false));
    }
}
