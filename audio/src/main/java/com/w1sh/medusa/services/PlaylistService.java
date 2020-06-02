package com.w1sh.medusa.services;

import com.w1sh.medusa.data.Playlist;
import com.w1sh.medusa.repos.PlaylistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlaylistService {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistService.class);

    private final UserService userService;
    private final TrackService trackService;
    private final PlaylistRepository repository;
    private final MemoryCache<String, List<Playlist>> cache;

    public PlaylistService(UserService userService, TrackService trackService, PlaylistRepository repository) {
        this.userService = userService;
        this.trackService = trackService;
        this.repository = repository;
        this.cache = new MemoryCacheBuilder<String, List<Playlist>>()
                .maximumSize(10000)
                .expireAfterAccess(Duration.ofHours(6))
                .defaultFetch(key -> repository.findAllByUserId(key).collectList())
                .build();
    }

    public Mono<Playlist> save(Playlist playlist){
        return userService.findByUserId(playlist.getUser().getUserId())
                .doOnNext(playlist::setUser)
                .flatMap(u -> trackService.saveAll(playlist.getTracks()))
                .flatMap(u -> repository.save(playlist))
                .onErrorResume(t -> Mono.fromRunnable(() -> logger.error("Failed to save playlist", t)))
                .flatMap(this::cache);
    }

    public Mono<List<Playlist>> findAllByUserId(String userId){
        return cache.get(userId)
                .onErrorResume(t -> Mono.fromRunnable(() -> logger.error("Failed to fetch playlists of user with id \"{}\"", userId, t)));
    }

    private Mono<Playlist> cache(Playlist playlist) {
        return cache.get(playlist.getUser().getUserId())
                .defaultIfEmpty(new ArrayList<>())
                .doOnNext(playlists -> {
                    playlists.remove(playlist);
                    playlists.add(playlist);
                    cache.put(playlist.getUser().getUserId(), playlists);
                })
                .then(Mono.just(playlist));
    }
}
