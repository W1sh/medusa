package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.data.Warning;
import com.w1sh.medusa.utils.Caches;
import com.w1sh.medusa.utils.Reactive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class WarningService {

    private final ReactiveMongoTemplate template;
    private final Cache<String, Set<Warning>> warnings;
    private final Cache<String, Warning> temporaryWarnings;

    public WarningService(ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory) {
        this.template = new ReactiveMongoTemplate(reactiveMongoDatabaseFactory);
        this.warnings = Caffeine.newBuilder().build();
        this.temporaryWarnings =  Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(30))
                .build();
    }

    public Mono<Warning> save(Warning warning) {
        warning.setCreatedOn(Instant.now());
        return template.save(warning)
                .doOnNext(w -> Caches.storeMultivalue(w.getUserId(), w, warnings.asMap().getOrDefault(w.getUserId(), new HashSet<>()), warnings))
                .flatMap(this::saveTemporary)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to save warning with id \"{}\"", warning.getId(), t)));
    }

    public Mono<Warning> saveTemporary(Warning warning) {
        return Mono.just(warning).doOnNext(w -> temporaryWarnings.put(w.getUserId(), w))
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to save temporary warning with id \"{}\"", warning.getId(), t)));
    }

    public Mono<Warning> addWarning(Warning warning){
        return Mono.justOrEmpty(temporaryWarnings.getIfPresent(warning.getUserId()))
                .hasElement()
                .transform(Reactive.ifElse(bool -> save(warning), bool -> saveTemporary(warning)));
    }
}
