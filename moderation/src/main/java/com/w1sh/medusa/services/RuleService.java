package com.w1sh.medusa.services;

import com.w1sh.medusa.data.Rule;
import com.w1sh.medusa.data.RuleEnum;
import com.w1sh.medusa.repos.RuleRepository;
import com.w1sh.medusa.services.cache.MemoryCache;
import com.w1sh.medusa.services.cache.MemoryCacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.EnumMap;
import java.util.Map;

@Service
@Slf4j
public class RuleService {

    private final RuleRepository ruleRepository;
    private final Map<RuleEnum, Integer> rules;
    private final MemoryCache<Integer, Rule> cache;

    public RuleService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
        this.rules = new EnumMap<>(RuleEnum.class);
        this.cache = new MemoryCacheBuilder<Integer, Rule>()
                .fetch(ruleRepository::findById)
                .build();

        loadAllRulesIntoCache();
    }

    public void loadAllRulesIntoCache(){
        ruleRepository.findAll()
                .doOnNext(rule -> cache.put(rule.getId(), rule))
                .doOnNext(rule -> rules.put(rule.getRuleValue(),rule.getId()))
                .count()
                .doOnSuccess(count -> log.info("Loaded {} rules into cache", count))
                .block();
    }

    public Mono<Rule> findByRuleEnum(RuleEnum ruleEnum){
        return cache.get(rules.get(ruleEnum));
    }

    public Mono<Rule> findById(Integer id){
        return cache.get(id);
    }


}
