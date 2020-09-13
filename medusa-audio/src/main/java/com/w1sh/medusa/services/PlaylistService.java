package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mongodb.client.result.DeleteResult;
import com.w1sh.medusa.data.Playlist;
import com.w1sh.medusa.repos.PlaylistRepository;
import com.w1sh.medusa.utils.Caches;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.cache.CacheMono;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Service
@Slf4j
public class PlaylistService {

    private final PlaylistRepository repository;
    private final Cache<String, List<Playlist>> cache;

    public PlaylistService(PlaylistRepository repository) {
        this.repository = repository;
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(6))
                .build();
    }

    public Mono<Playlist> save(Playlist playlist){
        return repository.save(playlist)
                .doOnNext(p -> Caches.storeMultivalue(p.getUserId(), p, cache.asMap().getOrDefault(p.getUserId(), new ArrayList<>()), cache))
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to save playlist", t)));
    }

    public Mono<List<Playlist>> findAllByUserId(String userId){
        final Supplier<Mono<List<Playlist>>> supplier = () -> repository.findAllByUserId(userId)
                .filter(Predicate.not(List::isEmpty));

        return CacheMono.lookup(key -> Mono.justOrEmpty(cache.getIfPresent(key))
                .map(Signal::next), userId)
                .onCacheMissResume(supplier)
                .andWriteWith((key, signal) -> Mono.fromRunnable(() -> Optional.ofNullable(signal.get()).ifPresent(value -> cache.put(key, value))))
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to fetch playlists of user with id \"{}\"", userId, t)));
    }

    public Mono<Boolean> deleteByUserId(String userId) {
        return repository.removeByUserId(userId)
                .doOnNext(ignored -> cache.invalidate(userId))
                .map(DeleteResult::wasAcknowledged);
    }

    public Mono<Boolean> deleteIndex(String userId, Integer index) {
        return findAllByUserId(userId)
                .flatMapIterable(Function.identity())
                .takeLast(index - 1)
                .next()
                .flatMap(repository::remove)
                .map(DeleteResult::wasAcknowledged)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete playlist and associated tracks", t)));
    }
}
