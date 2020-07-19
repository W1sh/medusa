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

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

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

        loadAllRulesIntoCache();
    }

    public void loadAllRulesIntoCache(){
        repository.findAll()
                .doOnNext(rule -> cache.put(rule.getId(), rule))
                .doOnNext(rule -> rules.put(rule.getRuleValue(),rule.getId()))
                .count()
                .doOnSuccess(count -> log.info("Loaded {} rules into cache", count))
                .block();
    }

    public Mono<Rule> findByRuleEnum(RuleEnum ruleEnum){
        return findById(rules.get(ruleEnum));
    }

    public Mono<Rule> findById(Integer id){
        return CacheMono.lookup(key -> Mono.justOrEmpty(cache.getIfPresent(key))
                .map(Signal::next), id)
                .onCacheMissResume(() -> repository.findById(id))
                .andWriteWith((key, signal) -> Mono.fromRunnable(() -> Optional.ofNullable(signal.get()).ifPresent(value -> cache.put(key, value))));
    }
}
