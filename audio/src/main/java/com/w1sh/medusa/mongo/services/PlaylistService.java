package com.w1sh.medusa.mongo.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.mongo.entities.Playlist;
import com.w1sh.medusa.mongo.repos.PlaylistRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public final class PlaylistService {

    private final PlaylistRepo playlistRepo;
    private final Cache<String, List<Playlist>> playlistsCache;

    public PlaylistService(PlaylistRepo playlistRepo) {
        this.playlistRepo = playlistRepo;
        this.playlistsCache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(6))
                .recordStats()
                .build();
    }

    public Flux<Playlist> findAll(){
        return playlistRepo.findAll();
    }

    public Mono<Playlist> save(Playlist playlistMono){
        return playlistRepo.save(playlistMono);
    }
}
