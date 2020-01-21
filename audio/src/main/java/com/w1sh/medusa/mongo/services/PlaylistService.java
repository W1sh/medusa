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
    private final Cache<Long, List<Playlist>> playlistsCache;

    public PlaylistService(PlaylistRepo playlistRepo) {
        this.playlistRepo = playlistRepo;
        this.playlistsCache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(6))
                .recordStats()
                .build();
    }

    public Flux<Playlist> findAllByUserId(Long userId){
        return CacheMono.lookup(key -> Mono.justOrEmpty(playlistsCache.getIfPresent(key))
                .map(Signal::next), userId)
                .onCacheMissResume(() -> Mono.just(new ArrayList<>()))
                .andWriteWith((key, signal) -> Mono.fromRunnable(
                        () -> Optional.ofNullable(signal.get())
                                .ifPresent(value -> playlistsCache.put(key, value))))
                .onErrorResume(throwable -> {
                    logger.error("Failed to fetch playlists of user with id \"{}\"", userId, throwable);
                    return Mono.empty();
                })
                .flatMapMany(Flux::fromIterable);
    }

    public Mono<Playlist> save(Playlist playlistMono){
        return findAllByUserId(playlistMono.getUser())
                .collectList()
                .map(playlists -> findAndReplace(playlists, playlistMono))
                .flatMapMany(Flux::fromIterable)
                .filter(playlist -> playlist.getId().equalsIgnoreCase(playlistMono.getId()))
                .next();
    }

    private List<Playlist> findAndReplace(List<Playlist> playlists, Playlist playlist){
        int index = -1;
        for(int i=0; i<playlists.size(); i++){
            if(playlist.getId().equalsIgnoreCase(playlists.get(i).getId())) index = i;
        }
        if(index >= 0) {
            playlists.set(index, playlist);
        } else {
            playlists.add(playlist);
        }
        playlistsCache.put(playlist.getUser(), playlists);
        return playlists;
    }

    public List<Playlist> removeIndex(List<Playlist> playlists, Integer index){
        Playlist playlist = playlists.get(index - 1);
        playlists.remove(playlist);
        playlistsCache.put(playlist.getUser(), playlists);
        return playlists;
    }
}
