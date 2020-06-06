package com.w1sh.medusa.services;

import com.w1sh.medusa.data.Playlist;
import com.w1sh.medusa.data.Track;
import com.w1sh.medusa.repos.TrackRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
public class TrackService {

    private final TrackRepository trackRepository;
    private final MemoryCache<Integer, Track> cache;

    public TrackService(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
        this.cache = new MemoryCacheBuilder<Integer, Track>()
                .maximumSize(10000)
                .expireAfterAccess(Duration.ofHours(6))
                .defaultFetch(key -> Mono.empty())
                .build();
    }

    public Mono<List<Track>> saveAll(List<Track> tracks){
        return trackRepository.saveAll(tracks)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to save tracks", t)))
                .doOnNext(u -> cache.put(u.getId(), u))
                .collectList();
    }

    public Mono<List<Track>> findAllByPlaylistId(Playlist playlist){
        return trackRepository.findAllByPlaylistId(playlist.getId())
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to retrieve all tracks from playlist {}", playlist.getId(), t)))
                .doOnNext(u -> cache.put(u.getId(), u))
                .collectList();
    }
}
