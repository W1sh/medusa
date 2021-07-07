package com.w1sh.medusa.hazelcast;

import com.austinv11.servicer.WireService;
import com.hazelcast.core.HazelcastInstance;
import discord4j.store.api.Store;
import discord4j.store.api.primitive.LongObjStore;
import discord4j.store.api.service.StoreService;
import discord4j.store.api.util.StoreContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@WireService(StoreService.class)
@ConditionalOnProperty(value = "medusa.hazelcast.enabled", havingValue = "true")
public final class HazelcastStoreService implements StoreService {

    private final HazelcastInstance instance;

    public HazelcastStoreService(HazelcastInstance instance) {
        this.instance = instance;
    }

    @Override
    public boolean hasGenericStores() {
        return true;
    }

    @NonNull
    @Override
    public <K extends Comparable<K>, V> Store<K, V> provideGenericStore(@NonNull Class<K> keyClass, Class<V> valueClass) {
        return new HazelcastStore<K, V>(instance.getMap(valueClass.getSimpleName()));
    }

    @Override
    public boolean hasLongObjStores() {
        return false;
    }

    @Override
    public <V> LongObjStore<V> provideLongObjStore(@NonNull Class<V> valueClass) {
        return null;
    }

    @Override
    public void init(@NonNull StoreContext context) {

    }

    @NonNull
    @Override
    public Mono<Void> dispose() {
        return Mono.empty();
    }
}
