package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.data.GuildUser;
import com.w1sh.medusa.mappers.Member2GuildUserMapper;
import com.w1sh.medusa.repos.GuildUserRepository;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.presence.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Service
public class GuildUserService {

    private static final Logger logger = LoggerFactory.getLogger(GuildUserService.class);

    private final GuildUserRepository repository;
    private final UserService userService;
    private final Cache<String, List<GuildUser>> guildUsersCache;
    private final Member2GuildUserMapper member2GuildUserMapper;

    @Value("${points.reward.amount}")
    private String rewardAmount;

    public GuildUserService(GuildUserRepository repository, UserService userService,
                            Member2GuildUserMapper member2GuildUserMapper) {
        this.repository = repository;
        this.userService = userService;
        this.member2GuildUserMapper = member2GuildUserMapper;
        this.guildUsersCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterAccess(Duration.ofHours(12))
                .recordStats()
                .build();
    }

    public Mono<GuildUser> save(GuildUser guildUser){
        return fetchUserByUserId(guildUser)
                .flatMap(repository::save)
                .onErrorResume(throwable -> {
                    logger.error("Failed to save guild user with id \"{}\"", guildUser.getId(), throwable);
                    return Mono.empty();
                })
                .doOnNext(this::saveInCache);
    }

    public Mono<GuildUser> findByUserIdAndGuildId(String userId, String guildId) {
        return fetchAllGuildUsersInGuild(guildId)
                .filterWhen(guildUser -> userService.findByUserId(userId)
                        .filter(user -> user.getId().equals(guildUser.getUser().getId()))
                        .doOnNext(guildUser::setUser)
                        .hasElement())
                .next()
                .switchIfEmpty(userService.findByUserId(userId)
                        .map(user -> new GuildUser(user, guildId)));
    }

    public Mono<Void> distributePointsInGuild(Guild guild) {
        return guild.getMembers()
                .filterWhen(this::isEligible)
                .map(member2GuildUserMapper::map)
                .flatMap(guildUser -> findByUserIdAndGuildId(guildUser.getUser().getUserId(), guildUser.getGuildId()))
                .doOnNext(u -> u.setPoints(u.getPoints() + Integer.parseInt(rewardAmount)))
                .concatMap(this::save)
                .then();
    }

    public Flux<GuildUser> findTop5PointsInGuild(String guildId){
        return repository.findAllByGuildIdOrderByPoints(guildId)
                .flatMap(this::fetchUserById)
                .collectList()
                .doOnNext(list -> guildUsersCache.put(guildId, list))
                .flatMapIterable(Function.identity())
                .take(5);
    }

    private void saveInCache(GuildUser user) {
        List<GuildUser> guildUsers = guildUsersCache.getIfPresent(user.getGuildId());

        if (guildUsers != null) {
            guildUsers.remove(user);
            guildUsers.add(user);
        } else {
            guildUsers = new ArrayList<>();
            guildUsers.add(user);
            guildUsersCache.put(user.getGuildId(), guildUsers);
        }
    }

    private Mono<GuildUser> fetchUserById(GuildUser guildUser){
        if(guildUser.getUser().getUserId() != null) return Mono.just(guildUser);
        return userService.findById(guildUser.getUser().getId())
                .doOnNext(guildUser::setUser)
                .then(Mono.just(guildUser));
    }

    private Mono<GuildUser> fetchUserByUserId(GuildUser guildUser) {
        if(guildUser.getUser().getId() != null) return Mono.just(guildUser);
        return userService.findByUserId(guildUser.getUser().getUserId())
                .doOnNext(guildUser::setUser)
                .then(Mono.just(guildUser));
    }

    private Flux<GuildUser> fetchAllGuildUsersInGuild(String guildId) {
        return CacheMono.lookup(key -> Mono.justOrEmpty(guildUsersCache.getIfPresent(key))
                .map(Signal::next), guildId)
                .onCacheMissResume(() -> repository.findAllByGuildId(guildId)
                        .collectList()
                        .filter(Predicate.not(List::isEmpty)))
                .andWriteWith((key, signal) ->
                        Mono.fromRunnable(() -> Optional.ofNullable(signal.get())
                                .ifPresent(value -> guildUsersCache.put(key, value))))
                .onErrorResume(throwable -> {
                    logger.error("Failed to retrieve all guild users in guild with guild id \"{}\"", guildId, throwable);
                    return Mono.empty();
                })
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
