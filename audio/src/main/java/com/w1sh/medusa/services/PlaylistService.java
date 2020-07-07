package com.w1sh.medusa.services;

import com.w1sh.medusa.data.Playlist;
import com.w1sh.medusa.repos.PlaylistRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Service
@Slf4j
public class PlaylistService {

    private final UserService userService;
    private final TrackService trackService;
    private final PlaylistTrackService playlistTrackService;
    private final PlaylistRepository repository;
    private final MemoryCache<String, List<Playlist>> cache;

    public PlaylistService(UserService userService, TrackService trackService, PlaylistTrackService playlistTrackService,
                           PlaylistRepository repository) {
        this.userService = userService;
        this.trackService = trackService;
        this.playlistTrackService = playlistTrackService;
        this.repository = repository;
        this.cache = new MemoryCacheBuilder<String, List<Playlist>>()
                .maximumSize(1000)
                .expireAfterAccess(Duration.ofHours(6))
                .fetch(key -> repository.findAllByUserId(key).collectList())
                .build();
    }

    public Mono<Playlist> save(Playlist playlist){
        return userService.findByUserId(playlist.getUser().getUserId())
                .doOnNext(playlist::setUser)
                .flatMap(ignored -> trackService.saveAll(playlist.getTracks()))
                .doOnNext(playlist::setTracks)
                .flatMap(ignored -> repository.save(playlist))
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to save playlist", t)))
                .flatMap(this::cache)
                .flatMapMany(playlistTrackService::save)
                .then(Mono.just(playlist));
    }

    public Mono<List<Playlist>> findAllByUserId(String userId){
        return cache.get(userId)
                .flatMapIterable(Function.identity())
                .flatMap(playlist -> trackService.findAllByPlaylistId(playlist)
                        .doOnNext(playlist::setTracks)
                        .then(Mono.just(playlist)))
                .collectList()
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to fetch playlists of user with id \"{}\"", userId, t)));
    }

    public Mono<Boolean> deleteIndex(String userId, Integer index) {
        return cache.get(userId)
                .map(playlists -> {
                    Playlist removedPlaylist = playlists.remove(index - 1);
                    cache.put(userId, playlists);
                    return removedPlaylist;
                })
                .flatMap(playlist -> {
                    Mono<Integer> deleteTracks = playlistTrackService.delete(playlist);
                    Mono<Void> deletePlaylist = repository.delete(playlist);
                    return deleteTracks.then(deletePlaylist).then(Mono.just(true));
                })
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete playlist and associated tracks", t)));
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
