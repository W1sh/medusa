package com.w1sh.medusa.services;

import com.w1sh.medusa.data.Playlist;
import com.w1sh.medusa.data.PlaylistTrack;
import com.w1sh.medusa.repos.PlaylistTrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaylistTrackService {

    private final PlaylistTrackRepository repository;

    public Flux<PlaylistTrack> save(Playlist playlist){
        return Flux.fromIterable(playlist.getTracks())
                .map(track -> new PlaylistTrack(playlist.getId(), track.getId()))
                .flatMap(this::save);
    }

    public Mono<PlaylistTrack> save(PlaylistTrack playlistTrack){
        return repository.save(playlistTrack)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to save playlist track", t)));
    }

    public Mono<Integer> delete(Playlist playlist){
        return repository.deleteAllByPlaylistId(playlist.getId())
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete playlist track", t)));
    }
}
