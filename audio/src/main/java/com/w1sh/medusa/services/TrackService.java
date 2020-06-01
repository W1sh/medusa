package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.data.Track;
import com.w1sh.medusa.repos.TrackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public class TrackService {

    private static final Logger logger = LoggerFactory.getLogger(TrackService.class);

    private final TrackRepository trackRepository;
    private final Cache<Integer, Track> trackCache;

    public TrackService(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
        this.trackCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterAccess(Duration.ofHours(12))
                .recordStats()
                .build();
    }

    public Mono<List<Track>> saveAll(List<Track> tracks){
        return trackRepository.saveAll(tracks)
                .onErrorResume(t -> Mono.fromRunnable(() -> logger.error("Failed to save tracks", t)))
                .doOnNext(u -> trackCache.put(u.getId(), u))
                .collectList();
    }
}
