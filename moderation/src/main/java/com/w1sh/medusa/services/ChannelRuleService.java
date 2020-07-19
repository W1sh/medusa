package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.data.ChannelRule;
import com.w1sh.medusa.data.Rule;
import com.w1sh.medusa.data.RuleEnum;
import com.w1sh.medusa.repos.ChannelRuleRepository;
import com.w1sh.medusa.utils.Caches;
import com.w1sh.medusa.utils.Reactive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.cache.CacheMono;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        return repository.save(channelRule)
                .doOnNext(cr -> Caches.storeMultivalue(cr.getChannel(), cr, cache.asMap().getOrDefault(cr.getChannel(), new ArrayList<>()), cache))
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to save channel rule with id \"{}\"", channelRule.getId(), t)));
    }

    public Mono<Boolean> delete(ChannelRule channelRule){
        return repository.delete(channelRule)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete channel rule with id \"{}\"", channelRule.getId(), t)))
                .then(Mono.just(true));
    }

    public Mono<Boolean> hasRule(String channelId, RuleEnum ruleEnum){
        return findByChannelAndRuleEnum(channelId, ruleEnum).hasElement();
    }

    public Mono<ChannelRule> findByChannelAndRule(String channelId, Rule rule){
        return findAllByChannel(channelId).transform(Reactive.findFirst(cr -> cr.getRule().getId().equals(rule.getId())));
    }

    public Mono<ChannelRule> findByChannelAndRuleEnum(String channelId, RuleEnum ruleEnum){
        return findAllByChannel(channelId).transform(Reactive.findFirst(cr -> cr.getRule().getRuleValue().equals(ruleEnum)));
    }

    public Mono<List<ChannelRule>> findAllByChannel(String channelId) {
        final Supplier<Mono<List<ChannelRule>>> supplier = () -> repository.findAllByChannel(channelId)
                .flatMap(channelRule -> ruleService.findById(channelRule.getRule().getId())
                        .doOnNext(channelRule::setRule)
                        .then(Mono.just(channelRule)))
                .collectList()
                .doOnSuccess(channelRules -> log.info("Fetched {} channel rules from database for channel with id {}", channelRules.size(), channelId));

        return CacheMono.lookup(key -> Mono.justOrEmpty(cache.getIfPresent(key))
                .map(Signal::next), channelId)
                .onCacheMissResume(supplier)
                .andWriteWith((key, signal) -> Mono.fromRunnable(() -> Optional.ofNullable(signal.get()).ifPresent(value -> cache.put(key, value))))
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to retrieve channel rules for channel with id \"{}\"", channelId, t)));
    }
}
