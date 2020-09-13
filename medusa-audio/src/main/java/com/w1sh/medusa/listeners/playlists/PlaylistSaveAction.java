package com.w1sh.medusa.listeners.playlists;

import com.w1sh.medusa.AudioConnection;
import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.data.Playlist;
import com.w1sh.medusa.data.Track;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.events.PlaylistEvent;
import com.w1sh.medusa.mappers.AudioTrack2TrackMapper;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.services.PlaylistService;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public final class PlaylistSaveAction implements Function<PlaylistEvent, Mono<Message>> {

    private final PlaylistService playlistService;
    private final MessageService messageService;
    private final AudioConnectionManager audioConnectionManager;
    private final AudioTrack2TrackMapper audioTrack2TrackMapper;

    @Override
    public Mono<Message> apply(PlaylistEvent event) {
        final Mono<List<Track>> tracks = audioConnectionManager.getAudioConnection(event)
                .flatMap(this::getAllTracks);

        return tracks.map(t -> createPlaylist(event, t))
                .flatMap(playlistService::save)
                .flatMap(playlist -> messageService.send(event.getChannel(), MessageEnum.PLAYLIST_SAVE_SUCCESS,
                        event.getNickname(), String.valueOf(playlist.getTracks().size())))
                .switchIfEmpty(messageService.send(event.getChannel(), MessageEnum.PLAYLIST_SAVE_ERROR, event.getNickname()))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> log.error("Failed to save playlist", throwable)));
    }

    private Playlist createPlaylist(PlaylistEvent event, List<Track> tracks){
        final String name = event.getArguments().size() > 1 ? event.getArguments().get(1) : "Playlist";
        return new Playlist(event.getUserId(), name, tracks);
    }

    private Mono<List<Track>> getAllTracks(AudioConnection audioConnection){
        return Flux.fromIterable(audioConnection.getTrackScheduler().getFullQueue())
                .map(audioTrack2TrackMapper::map)
                .collectList();
    }
}
