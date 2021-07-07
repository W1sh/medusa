package com.w1sh.medusa.hazelcast;

import com.hazelcast.map.IMap;
import discord4j.store.api.Store;
import discord4j.store.api.util.WithinRangePredicate;
import org.reactivestreams.Publisher;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.concurrent.TimeUnit;

public final class HazelcastStore<K extends Comparable<K>, V> implements Store<K,V> {

    public static final int TTL = 60;

    private final IMap<K, V> cache;

    public HazelcastStore(IMap<K, V> cache) {
        this.cache = cache;
    }

    @NonNull
    @Override
    public Mono<Void> save(@NonNull K key, @NonNull V value) {
        return Mono.fromRunnable(() -> cache.set(key, value, TTL, TimeUnit.MINUTES));
    }

    @NonNull
    @Override
    public Mono<Void> save(@NonNull Publisher<Tuple2<K, V>> entryStream) {
        return Flux.from(entryStream).doOnNext(t -> save(t.getT1(), t.getT2())).then();
    }

    @NonNull
    @Override
    public Mono<Void> delete(@NonNull K id) {
        return Mono.fromRunnable(() -> cache.delete(id));
    }

    @NonNull
    @Override
    public Mono<Void> delete(@NonNull Publisher<K> ids) {
        return Flux.from(ids).doOnNext(cache::delete).then();
    }

    @NonNull
    @Override
    public Mono<Void> deleteInRange(@NonNull K start, @NonNull K end) {
        return keys().filter(new WithinRangePredicate<>(start, end)).doOnNext(cache::delete).then();
    }

    @NonNull
    @Override
    public Mono<Void> deleteAll() {
        return Mono.fromRunnable(cache::clear);
    }

    @NonNull
    @Override
    public Mono<Void> invalidate() {
        return Mono.fromRunnable(cache::clear);
    }

    @NonNull
    @Override
    public Mono<V> find(@NonNull K id) {
        return Mono.fromCallable(() -> cache.getOrDefault(id, null));
    }

    @NonNull
    @Override
    public Flux<V> findInRange(@NonNull K start, @NonNull K end) {
        return keys().filter(new WithinRangePredicate<>(start, end)).flatMap(this::find);
    }

    @NonNull
    @Override
    public Mono<Long> count() {
        return Mono.fromCallable(() -> Long.valueOf(cache.size()));
    }

    @NonNull
    @Override
    public Flux<K> keys() {
        return Flux.fromIterable(cache.keySet());
    }

    @NonNull
    @Override
    public Flux<V> values() {
        return Flux.fromIterable(cache.values());
    }
}
