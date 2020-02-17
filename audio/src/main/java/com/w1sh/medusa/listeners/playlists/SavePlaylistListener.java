package com.w1sh.medusa.listeners.playlists;

import com.w1sh.medusa.AudioConnection;
import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.TrackScheduler;
import com.w1sh.medusa.data.Playlist;
import com.w1sh.medusa.data.Track;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.playlists.SavePlaylistEvent;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.services.PlaylistService;
import discord4j.core.object.entity.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public final class SavePlaylistListener implements EventListener<SavePlaylistEvent> {

    private static final Logger logger = LoggerFactory.getLogger(SavePlaylistListener.class);

    private final PlaylistService playlistService;
    private final ResponseDispatcher responseDispatcher;
    private final AudioConnectionManager audioConnectionManager;

    public SavePlaylistListener(ResponseDispatcher responseDispatcher, PlaylistService playlistService, AudioConnectionManager audioConnectionManager) {
        this.responseDispatcher = responseDispatcher;
        this.playlistService = playlistService;
        this.audioConnectionManager = audioConnectionManager;
    }

    @Override
    public Class<SavePlaylistEvent> getEventType() {
        return SavePlaylistEvent.class;
    }

    @Override
    public Mono<Void> execute(SavePlaylistEvent event) {

        return Mono.justOrEmpty(event.getGuildId())
                .flatMap(audioConnectionManager::getAudioConnection)
                .map(AudioConnection::getTrackScheduler)
                .flatMapIterable(TrackScheduler::getFullQueue)
                .map(at -> new Track(at.getInfo().author, at.getInfo().title, at.getInfo().uri, at.getInfo().length))
                .collectList()
                .map(tracks -> createPlaylist(event, tracks))
                .flatMap(playlistService::save)
                .onErrorResume(throwable -> Mono.fromRunnable(() -> logger.error("Failed to save playlist", throwable)))
                .flatMap(playlist -> createSavePlaylistSuccessMessage(event, playlist))
                .switchIfEmpty(createFailedSaveErrorMessage(event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Playlist createPlaylist(SavePlaylistEvent event, List<Track> tracks){
        Long userId = event.getMember().map(member -> member.getId().asLong()).orElseThrow();
        String name = event.getArguments().getOrDefault(0, "Playlist");

        return new Playlist(userId, name, tracks);
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
