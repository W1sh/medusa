package com.w1sh.medusa.api.moderation.services;

import com.w1sh.medusa.api.moderation.data.ChannelRule;
import com.w1sh.medusa.api.moderation.repos.ChannelRuleRepository;
import com.w1sh.medusa.services.cache.MemoryCache;
import com.w1sh.medusa.services.cache.MemoryCacheBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

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

    public Mono<Void> delete(ChannelRule channelRule){
        return repository.delete(channelRule);
    }

    public Mono<List<ChannelRule>> findAllByChannel(String channelId){
        return cache.get(channelId)
                .doOnNext(channelRules -> cache.put(channelId, channelRules));
    }

    private Mono<ChannelRule> fetchRules(ChannelRule channelRule){
        return ruleService.findById(channelRule.getRule().getId())
                .doOnNext(channelRule::setRule)
                .then(Mono.just(channelRule));
    }
}
