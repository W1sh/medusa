package com.w1sh.medusa.mongo.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.mongo.entities.Playlist;
import com.w1sh.medusa.mongo.repos.PlaylistRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.cache.CacheMono;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public final class PlaylistService {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistService.class);

    private final PlaylistRepo playlistRepo;
    private final Cache<String, List<Playlist>> playlistsCache;

    public PlaylistService(PlaylistRepo playlistRepo) {
        this.playlistRepo = playlistRepo;
        this.playlistsCache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(6))
                .recordStats()
                .build();
    }

    public Flux<Playlist> findAll(String playlistsKey){
        return CacheMono.lookup(key -> Mono.justOrEmpty(playlistsCache.getIfPresent(key))
                .map(Signal::next), playlistsKey)
                .onCacheMissResume(Mono::empty)
                .andWriteWith((key, signal) -> Mono.fromRunnable(
                        () -> Optional.ofNullable(signal.get())
                                .ifPresent(value -> playlistsCache.put(key, value))))
                .onErrorResume(throwable -> {
                    logger.error("Failed to fetch playlists of user with id \"{}\"", playlistsKey, throwable);
                    return Mono.just(new ArrayList<>());
                })
                .flatMapMany(Flux::fromIterable);
    }

    public Mono<Playlist> save(Playlist playlistMono){
        return playlistRepo.save(playlistMono);
    }
}
