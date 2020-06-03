package com.w1sh.medusa.services;

import com.w1sh.medusa.data.Playlist;
import com.w1sh.medusa.data.PlaylistTrack;
import com.w1sh.medusa.repos.PlaylistTrackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PlaylistTrackService {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistTrackService.class);

    private final PlaylistTrackRepository repository;

    public PlaylistTrackService(PlaylistTrackRepository repository) {
        this.repository = repository;
    }

    public Flux<PlaylistTrack> save(Playlist playlist){
        return Flux.fromIterable(playlist.getTracks())
                .map(track -> new PlaylistTrack(playlist.getId(), track.getId()))
                .flatMap(this::save);
    }

    public Mono<PlaylistTrack> save(PlaylistTrack playlistTrack){
        return repository.save(playlistTrack)
                .onErrorResume(t -> Mono.fromRunnable(() -> logger.error("Failed to save playlist track", t)));
    }
}
