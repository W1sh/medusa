package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.data.User;
import com.w1sh.medusa.mappers.Member2UserMapper;
import com.w1sh.medusa.repos.UserRepository;
import com.w1sh.medusa.utils.Caches;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.presence.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.cache.CacheMono;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Service
@Slf4j
public class UserService {

    private final UserRepository repository;
    private final Cache<String, List<User>> cache;
    private final Member2UserMapper member2UserMapper;

    @Value("${points.reward.amount}")
    private String rewardAmount;

    public UserService(UserRepository repository, Member2UserMapper member2UserMapper) {
        this.repository = repository;
        this.member2UserMapper = member2UserMapper;
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(6))
                .build();
    }

    public Mono<User> save(User user){
        return repository.save(user)
                .doOnNext(u -> Caches.storeMultivalue(u.getGuildId(), u, cache.asMap().getOrDefault(u.getGuildId(), new ArrayList<>()), cache))
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to save guild user with id \"{}\"", user.getId(), t)));
    }

    public Mono<User> findByUserIdAndGuildId(String userId, String guildId) {
        return fetchAllUsersInGuild(guildId)
                .filter(user -> userId.equals(user.getUserId()))
                .next()
                .defaultIfEmpty(new User(userId, guildId));
    }

    public Mono<Long> distributePointsInGuild(Guild guild) {
        return guild.getMembers()
                .filterWhen(this::isEligible)
                .map(member2UserMapper::map)
                .flatMap(guildUser -> findByUserIdAndGuildId(guildUser.getUserId(), guildUser.getGuildId()))
                .doOnNext(u -> u.setPoints(u.getPoints() + Integer.parseInt(rewardAmount)))
                .concatMap(this::save)
                .count();
    }

    public Flux<User> findTop5PointsInGuild(String guildId){
        return repository.findTop5ByGuildIdOrderByPoints(guildId);
    }

    private Flux<User> fetchAllUsersInGuild(String guildId) {
        final Supplier<Mono<List<User>>> supplier = () -> repository.findAllByGuildId(guildId)
                .filter(Predicate.not(List::isEmpty));

        return CacheMono.lookup(key -> Mono.justOrEmpty(cache.getIfPresent(key))
                .map(Signal::next), guildId)
                .onCacheMissResume(supplier)
                .andWriteWith((key, signal) -> Mono.fromRunnable(() -> Optional.ofNullable(signal.get()).ifPresent(value -> cache.put(key, value))))
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to retrieve all guild users in guild with guild id \"{}\"", guildId, t)))
                .flatMapIterable(Function.identity());
    }

    private Mono<Boolean> isEligible(Member member) {
        return Mono.just(member)
                .filter(m -> !m.isBot())
                .flatMap(Member::getPresence)
                .map(Presence::getStatus)
                .filter(status -> status.equals(Status.ONLINE) || status.equals(Status.IDLE)
                        || status.equals(Status.DO_NOT_DISTURB))
                .hasElement();
    }
}
