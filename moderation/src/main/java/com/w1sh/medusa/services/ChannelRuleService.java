package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mongodb.client.result.DeleteResult;
import com.w1sh.medusa.data.Channel;
import com.w1sh.medusa.repos.ChannelRuleRepository;
import com.w1sh.medusa.utils.Reactive;
import discord4j.core.object.entity.channel.GuildChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.cache.CacheMono;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.util.Optional;
import java.util.function.Supplier;

@Service
@Slf4j
public class ChannelRuleService {

    private final ChannelRuleRepository repository;
    private final Cache<String, Channel> cache;

    public ChannelRuleService(ChannelRuleRepository repository) {
        this.repository = repository;
        this.cache = Caffeine.newBuilder().build();
    }

    public Mono<Channel> save(Channel channel){
        return Mono.justOrEmpty(channel)
                .flatMap(repository::save)
                .doOnNext(cr -> cache.put(cr.getId(), cr))
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to save channel rule for channel with id \"{}\"", channel.getChannelId(), t)));
    }

    public Mono<Boolean> delete(Channel channel){
        final Mono<Boolean> deleteMono = Mono.defer(() -> repository.remove(channel)
                .map(DeleteResult::wasAcknowledged)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete channel rule with id \"{}\"", channel.getId(), t))));

        final Mono<Boolean> saveMono = Mono.defer(() -> save(channel).hasElement());

        return Mono.justOrEmpty(channel)
                .filter(chan -> chan.getRules().isEmpty())
                .hasElement()
                .transform(Reactive.ifElse(bool -> deleteMono, bool -> saveMono));
    }

    public Mono<Channel> findByChannel(GuildChannel channel) {
        final Supplier<Mono<Channel>> supplier = () -> repository.findByChannel(channel.getId().asString())
                .doOnTerminate(() -> log.info("Fetched channel rules from database for channel with id {}", channel.getId().asString()))
                .defaultIfEmpty(new Channel(channel.getId().asString(), channel.getGuildId().asString()));

        return CacheMono.lookup(key -> Mono.justOrEmpty(cache.getIfPresent(key))
                .map(Signal::next), channel.getId().asString())
                .onCacheMissResume(supplier)
                .andWriteWith((key, signal) -> Mono.fromRunnable(() -> Optional.ofNullable(signal.get()).ifPresent(value -> cache.put(key, value))))
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to retrieve channel rules for channel with id \"{}\"", channel.getId().asString(), t)));
    }
}
