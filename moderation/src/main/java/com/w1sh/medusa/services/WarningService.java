package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.data.Warning;
import com.w1sh.medusa.repos.WarningRepository;
import com.w1sh.medusa.utils.Reactive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.cache.CacheMono;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WarningService {

    private final WarningRepository repository;
    private final UserService userService;
    private final Cache<Integer, Set<Warning>> warnings;
    private final Cache<Integer, Warning> temporaryWarnings;

    public WarningService(WarningRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
        this.warnings = Caffeine.newBuilder().build();
        this.temporaryWarnings =  Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(30))
                .build();
    }

    public Mono<Warning> save(Warning warning) {
        return repository.save(warning)
                .flatMap(this::cache);
    }

    public Mono<Warning> saveTemporary(Warning warning) {
        temporaryWarnings.put(warning.getUser().getId(), warning);
        return Mono.just(warning);
    }

    public Mono<Warning> addWarning(String userId, String channelId){
        Mono<Warning> warning = Mono.defer(() -> userService.findByUserId(userId)
                .map(user -> new Warning(user, channelId)));

        return userService.findByUserId(userId)
                .filterWhen(user -> Mono.justOrEmpty(temporaryWarnings.getIfPresent(user.getId())).hasElement())
                .hasElement()
                .transform(Reactive.ifElse(bool -> warning.flatMap(this::save), bool -> warning.flatMap(this::saveTemporary)));
    }

    private Mono<Set<Warning>> findAllByUser(Integer userId){
        final Supplier<Mono<Set<Warning>>> supplier = () -> repository.findAllByUser(userId)
                .collect(Collectors.toSet())
                .doOnNext(ws -> log.info("Fetched {} warnings from database for user with id {}", ws.size(), userId));

        return CacheMono.lookup(key -> Mono.justOrEmpty(warnings.getIfPresent(key))
                .map(Signal::next), userId)
                .onCacheMissResume(supplier)
                .andWriteWith((key, signal) -> Mono.fromRunnable(() -> Optional.ofNullable(signal.get()).ifPresent(value -> warnings.put(key, value))));
    }

    private Mono<Warning> cache(Warning warning) {
        return Mono.justOrEmpty(warnings.getIfPresent(warning.getUser().getId()))
                .doOnNext(ws -> ws.add(warning))
                .doOnNext(ws -> warnings.put(warning.getUser().getId(), ws))
                .then(Mono.just(warning));
    }


}
