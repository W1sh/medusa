package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.data.Playlist;
import com.w1sh.medusa.repos.PlaylistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.cache.CacheMono;
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
    private final TrackService trackService;
    private final PlaylistRepository repository;
    private final Cache<String, List<Playlist>> playlistsCache;

    public PlaylistService(UserService userService, TrackService trackService, PlaylistRepository repository) {
        this.userService = userService;
        this.trackService = trackService;
        this.repository = repository;
        this.playlistsCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterAccess(Duration.ofHours(6))
                .recordStats()
                .build();
    }

    public Mono<Playlist> save(Playlist playlist){
        return userService.findByUserId(playlist.getUser().getUserId())
                .doOnNext(playlist::setUser)
                .flatMap(u -> trackService.saveAll(playlist.getTracks()))
                .flatMap(u -> repository.save(playlist))
                .onErrorResume(throwable -> {
                    logger.error("Failed to save playlist", throwable);
                    return Mono.empty();
                })
                .flatMap(this::cache);
    }

    public Mono<List<Playlist>> findAllByUserId(String userId){
        return CacheMono.lookup(key -> Mono.justOrEmpty(playlistsCache.getIfPresent(key))
                .map(Signal::next), userId)
                .onCacheMissResume(() -> repository.findAllByUserId(userId).collectList())
                .andWriteWith((key, signal) -> Mono.fromRunnable(
                        () -> Optional.ofNullable(signal.get()).ifPresent(value -> playlistsCache.put(key, value))))
                .onErrorResume(throwable -> {
                    logger.error("Failed to fetch playlists of user with id \"{}\"", userId, throwable);
                    return Mono.empty();
                });
    }

    private Mono<Playlist> cache(Playlist playlist) {
        return findAllByUserId(playlist.getUser().getUserId())
                .defaultIfEmpty(new ArrayList<>())
                .doOnNext(playlists -> {
                    playlists.remove(playlist);
                    playlists.add(playlist);
                    playlistsCache.put(playlist.getUser().getUserId(), playlists);
                })
                .then(Mono.just(playlist));
    }
}
