package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.data.ChannelRule;
import com.w1sh.medusa.data.Rule;
import com.w1sh.medusa.data.RuleEnum;
import com.w1sh.medusa.repos.ChannelRuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.cache.CacheMono;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
@Slf4j
public class ChannelRuleService {

    private final ChannelRuleRepository repository;
    private final RuleService ruleService;
    private final Cache<String, List<ChannelRule>> cache;

    public ChannelRuleService(ChannelRuleRepository repository, RuleService ruleService) {
        this.repository = repository;
        this.ruleService = ruleService;
        this.cache = Caffeine.newBuilder().build();
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

    public Mono<Boolean> hasRule(String channelId, RuleEnum ruleEnum){
        return findByChannelAndRuleEnum(channelId, ruleEnum).hasElement();
    }

    private Mono<ChannelRule> findByChannelAndRuleEnum(String channelId, RuleEnum ruleEnum){
        return findAllByChannel(channelId)
                .flatMapIterable(Function.identity())
                .filter(channelRule -> channelRule.getRule().getRuleValue().equals(ruleEnum))
                .next();
    }

    private Mono<ChannelRule> fetchRules(ChannelRule channelRule){
        return ruleService.findById(channelRule.getRule().getId())
                .doOnNext(channelRule::setRule)
                .then(Mono.just(channelRule));
    }

    public Mono<List<ChannelRule>> findAllByChannel(String channelId) {
        final Supplier<Mono<List<ChannelRule>>> supplier = () -> repository.findAllByChannel(channelId)
                .flatMap(this::fetchRules)
                .collectList()
                .doOnSuccess(channelRules -> log.info("Fetched {} channel rules from database for channel with id {}", channelRules.size(), channelId));

        return CacheMono.lookup(key -> Mono.justOrEmpty(cache.getIfPresent(key))
                .map(Signal::next), channelId)
                .onCacheMissResume(supplier)
                .andWriteWith((key, signal) -> Mono.fromRunnable(() -> Optional.ofNullable(signal.get()).ifPresent(value -> cache.put(key, value))));
    }
}
