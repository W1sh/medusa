package com.w1sh.medusa.api.moderation.services;

import com.w1sh.medusa.api.moderation.data.Rule;
import com.w1sh.medusa.api.moderation.repos.RuleRepository;
import com.w1sh.medusa.services.cache.MemoryCache;
import com.w1sh.medusa.services.cache.MemoryCacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RuleService {

    private final RuleRepository ruleRepository;
    private final MemoryCache<Integer, Rule> cache;

    public RuleService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
        this.cache = new MemoryCacheBuilder<Integer, Rule>().build();
    }

    public void loadAllRulesIntoCache(){
        ruleRepository.findAll()
                .doOnNext(rule -> cache.put(rule.getId(), rule))
                .count()
                .doOnSuccess(count -> log.info("Loaded {} rules into cache", count))
                .block();
    }

    public Mono<Rule> findById(Integer id){
        return cache.get(id);
    }
}
