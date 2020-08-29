package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mongodb.client.result.DeleteResult;
import com.w1sh.medusa.data.Warning;
import com.w1sh.medusa.repos.WarningRepository;
import com.w1sh.medusa.utils.Caches;
import com.w1sh.medusa.utils.Reactive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class WarningService {

    private final WarningRepository repository;
    private final Cache<String, Set<Warning>> warnings;
    private final Cache<String, Warning> temporaryWarnings;

    public WarningService(WarningRepository repository){
        this.repository = repository;
        this.warnings = Caffeine.newBuilder().build();
        this.temporaryWarnings = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(30))
                .build();
    }

    public Mono<Warning> save(Warning warning) {
        return repository.save(warning)
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

    public Mono<Boolean> deleteByUserId(String userId) {
        return repository.removeByUserId(userId)
                .doOnNext(ignored -> warnings.invalidate(userId))
                .map(DeleteResult::wasAcknowledged);
    }

    public Mono<Boolean> deleteByChannelId(String channelId) {
        return repository.removeByChannelId(channelId)
                .doOnNext(ignored -> warnings.invalidateAll())
                .map(DeleteResult::wasAcknowledged);
    }

    public Mono<Boolean> deleteByGuildId(String guildId) {
        return repository.removeByGuildId(guildId)
                .doOnNext(ignored -> warnings.invalidateAll())
                .map(DeleteResult::wasAcknowledged);
    }
}
