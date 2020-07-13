package com.w1sh.medusa.services;

import com.w1sh.medusa.data.ChannelRule;
import com.w1sh.medusa.data.Rule;
import com.w1sh.medusa.repos.ChannelRuleRepository;
import com.w1sh.medusa.services.cache.MemoryCache;
import com.w1sh.medusa.services.cache.MemoryCacheBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@Service
public class ChannelRuleService {

    private final ChannelRuleRepository repository;
    private final RuleService ruleService;
    private final MemoryCache<String, List<ChannelRule>> cache;

    public ChannelRuleService(ChannelRuleRepository repository, RuleService ruleService) {
        this.repository = repository;
        this.ruleService = ruleService;
        this.cache = new MemoryCacheBuilder<String, List<ChannelRule>>()
                .fetch(channel -> repository.findAllByChannel(channel)
                        .flatMap(this::fetchRules)
                        .collectList())
                .build();
    }

    public Mono<ChannelRule> save(ChannelRule channelRule){
        return repository.save(channelRule);
    }

    public Mono<Boolean> delete(ChannelRule channelRule){
        return repository.delete(channelRule)
                .then(Mono.just(true));
    }

    public Mono<ChannelRule> findByChannelAndRule(String channelId, Rule rule){
        return findAllByChannel(channelId)
                .flatMapIterable(Function.identity())
                .filter(channelRule -> channelRule.getRule().getId().equals(rule.getId()))
                .next();
    }

    public Mono<List<ChannelRule>> findAllByChannel(String channelId) {
        return cache.get(channelId);
    }

    private Mono<ChannelRule> fetchRules(ChannelRule channelRule){
        return ruleService.findById(channelRule.getRule().getId())
                .doOnNext(channelRule::setRule)
                .then(Mono.just(channelRule));
    }
}
