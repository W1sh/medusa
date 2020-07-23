package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mongodb.client.result.DeleteResult;
import com.w1sh.medusa.data.ChannelRule;
import com.w1sh.medusa.data.Rule;
import com.w1sh.medusa.repos.ChannelRuleRepository;
import com.w1sh.medusa.utils.Caches;
import com.w1sh.medusa.utils.Reactive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.bool.BooleanUtils;
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
    private final Cache<String, List<ChannelRule>> cache;

    public ChannelRuleService(ChannelRuleRepository repository) {
        this.repository = repository;
        this.cache = Caffeine.newBuilder().build();
    }

    public Mono<ChannelRule> save(ChannelRule channelRule){
        return Mono.justOrEmpty(channelRule)
                .filterWhen(cr -> BooleanUtils.not(findByChannelAndRule(channelRule.getChannel(), channelRule.getRule()).hasElement()))
                .flatMap(repository::save)
                .doOnNext(cr -> Caches.storeMultivalue(cr.getChannel(), cr, cache.asMap().getOrDefault(cr.getChannel(), new ArrayList<>()), cache))
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to save channel rule with id \"{}\"", channelRule.getId(), t)));
    }

    public Mono<Boolean> delete(ChannelRule channelRule){
        return repository.remove(channelRule)
                .map(DeleteResult::wasAcknowledged)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete channel rule with id \"{}\"", channelRule.getId(), t)));
    }

    public Mono<ChannelRule> findByChannelAndRule(String channelId, Rule rule){
        return findAllByChannel(channelId).transform(Reactive.findFirst(cr -> cr.getRule().equals(rule)));
    }

    public Mono<List<ChannelRule>> findAllByChannel(String channelId) {
        final Supplier<Mono<List<ChannelRule>>> supplier = () -> repository.findAllByChannel(channelId)
                .collectList()
                .doOnSuccess(channelRules -> log.info("Fetched {} channel rules from database for channel with id {}", channelRules.size(), channelId));

        return CacheMono.lookup(key -> Mono.justOrEmpty(cache.getIfPresent(key))
                .map(Signal::next), channelId)
                .onCacheMissResume(supplier)
                .andWriteWith((key, signal) -> Mono.fromRunnable(() -> Optional.ofNullable(signal.get()).ifPresent(value -> cache.put(key, value))))
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to retrieve channel rules for channel with id \"{}\"", channelId, t)));
    }
}
