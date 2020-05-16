package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.data.GuildUser;
import com.w1sh.medusa.data.User;
import com.w1sh.medusa.repos.GuildUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.cache.CacheFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Service
public class GuildUserService {

    private static final Logger logger = LoggerFactory.getLogger(GuildUserService.class);

    private final GuildUserRepository repository;
    private final UserService userService;
    private final Cache<String, Object> guildUsersCache;

    @Value("${points.reward.amount}")
    private String rewardAmount;

    public GuildUserService(GuildUserRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
        this.guildUsersCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterAccess(Duration.ofHours(1))
                .recordStats()
                .build();
    }

    public Mono<GuildUser> save(GuildUser user){
        return repository.save(user)
                .onErrorResume(throwable -> {
                    logger.error("Failed to save user with id \"{}\"", user.getId(), throwable);
                    return Mono.empty();
                })
                .doOnNext(this::saveInCache);
    }

    public Mono<GuildUser> findByUserIdAndGuildId(String userId, String guildId) {
        Mono<User> user = userService.findByUserId(userId);

        return CacheFlux.lookup(guildUsersCache.asMap(), guildId, GuildUser.class)
                .onCacheMissResume(() -> repository.findByGuildId(guildId))
                .collectList()
                .doOnNext(list -> guildUsersCache.put(guildId, list))
                .flatMapIterable(Function.identity())
                .filterWhen(guildUser -> Mono.just(guildId)
                        .zipWith(user, ((s, u) -> guildUser.getGuildId().equals(s) && guildUser.getUser().getId().equals(u.getId()))))
                .next();
    }

    public Mono<Void> distributePoints(GuildUser user) {
        return findByUserIdAndGuildId(user.getUser().getUserId(), user.getGuildId())
                .doOnNext(u -> u.setPoints(u.getPoints() + Integer.parseInt(rewardAmount)))
                .flatMap(this::save)
                .then();
    }

    public Flux<GuildUser> findTop5Points(){
        return repository.findAllOrderByPoints().
                take(5);
    }

    private void saveInCache(GuildUser user) {
        List<GuildUser> guildUsers = (List<GuildUser>) guildUsersCache.getIfPresent(user.getGuildId());

        if (guildUsers != null) {
            guildUsers.remove(user);
            guildUsers.add(user);
        } else {
            guildUsers = new ArrayList<>();
            guildUsers.add(user);
            guildUsersCache.put(user.getGuildId(), guildUsers);
        }
    }
}
