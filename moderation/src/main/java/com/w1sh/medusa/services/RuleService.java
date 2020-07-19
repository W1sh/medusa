package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.data.Rule;
import com.w1sh.medusa.data.RuleEnum;
import com.w1sh.medusa.repos.RuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.cache.CacheMono;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.core.scheduler.Schedulers;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Slf4j
public class RuleService {

    private final RuleRepository repository;
    private final Map<RuleEnum, Integer> rules;
    private final Cache<Integer, Rule> cache;

    public RuleService(RuleRepository repository) {
        this.repository = repository;
        this.rules = new EnumMap<>(RuleEnum.class);
        this.cache = Caffeine.newBuilder().build();

        Schedulers.single().schedule(this::loadAllRulesIntoCache);
    }

    public Mono<Rule> findById(Integer id){
        final Supplier<Mono<Rule>> supplier = () -> repository.findById(id)
                .doOnSuccess(rule -> log.info("Fetched rule with id \"{}\" from database", rule.getId()));

        return CacheMono.lookup(key -> Mono.justOrEmpty(cache.getIfPresent(key))
                .map(Signal::next), id)
                .onCacheMissResume(supplier)
                .andWriteWith((key, signal) -> Mono.fromRunnable(() -> Optional.ofNullable(signal.get()).ifPresent(value -> cache.put(key, value))))
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to retrieve rule with id \"{}\"", id, t)));
    }

    public Mono<Rule> findByRuleEnum(RuleEnum ruleEnum){
        return findById(rules.get(ruleEnum));
    }

    private void loadAllRulesIntoCache(){
        repository.findAll()
                .doOnNext(rule -> cache.put(rule.getId(), rule))
                .doOnNext(rule -> rules.put(rule.getRuleValue(),rule.getId()))
                .count()
                .doOnSuccess(count -> log.info("Loaded {} rules into cache", count))
                .subscribe();
    }
}
