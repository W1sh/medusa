package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.data.Playlist;
import com.w1sh.medusa.repos.PlaylistRepository;
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
public class PlaylistService {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistService.class);

    private final UserService userService;
    private final PlaylistRepository repository;
    private final Cache<String, List<Playlist>> playlistsCache;

    public PlaylistService(UserService userService, PlaylistRepository repository) {
        this.userService = userService;
        this.repository = repository;
        this.playlistsCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterAccess(Duration.ofHours(6))
                .recordStats()
                .build();
    }

    public Flux<Playlist> findAllByUserId(String userId){
        return CacheMono.lookup(key -> Mono.justOrEmpty(playlistsCache.getIfPresent(key))
                .map(Signal::next), userId)
                .onCacheMissResume(() -> userService.findByUserId(userId)
                        .flatMapMany(user -> repository.findAllByUserId(user.getId()))
                        .collectList())
                .andWriteWith((key, signal) -> Mono.fromRunnable(
                        () -> Optional.ofNullable(signal.get()).ifPresent(value -> playlistsCache.put(key, value))))
                .onErrorResume(throwable -> {
                    logger.error("Failed to fetch playlists of user with id \"{}\"", userId, throwable);
                    return Mono.empty();
                })
                .flatMapMany(Flux::fromIterable);
    }

    public Mono<Playlist> save(Playlist playlist){
        return userService.findByUserId(playlist.getUser().getUserId())
                .doOnNext(playlist::setUser)
                .flatMap(u -> repository.save(playlist))
                .onErrorResume(throwable -> {
                    logger.error("Failed to save playlist", throwable);
                    return Mono.empty();
                })
                .doOnNext(this::cache);
    }

    private void cache(Playlist playlist) {
        List<Playlist> playlists = playlistsCache.getIfPresent(playlist.getUser().getUserId());

        if (playlists != null) {
            playlists.remove(playlist);
            playlists.add(playlist);
        } else {
            playlists = new ArrayList<>();
            playlists.add(playlist);
            playlistsCache.put(playlist.getUser().getUserId(), playlists);
        }
    }
}
