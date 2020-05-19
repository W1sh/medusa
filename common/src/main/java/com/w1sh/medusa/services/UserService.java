package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.data.User;
import com.w1sh.medusa.repos.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.cache.CacheMono;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final Cache<String, User> usersCache;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.usersCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterAccess(Duration.ofHours(12))
                .recordStats()
                .build();
    }

    public Mono<User> save(User user){
        return userRepository.save(user)
                .onErrorResume(throwable -> {
                    logger.error("Failed to save user with id \"{}\"", user.getId(), throwable);
                    return Mono.empty();
                })
                .doOnNext(u -> usersCache.put(u.getUserId(), u));
    }

    public Mono<User> findByUserId(String userId) {
        return CacheMono.lookup(key -> Mono.justOrEmpty(usersCache.getIfPresent(key))
                .map(Signal::next), userId)
                .onCacheMissResume(() -> userRepository.findByUserId(userId)
                        .switchIfEmpty(save(new User(userId)))
                        .subscribeOn(Schedulers.elastic()))
                .andWriteWith((key, signal) -> Mono.fromRunnable(
                        () -> Optional.ofNullable(signal.get())
                                .ifPresent(value -> usersCache.put(key, value))))
                .onErrorResume(throwable -> {
                    logger.error("Failed to retrieve user with id \"{}\"", userId, throwable);
                    return Mono.empty();
                });
    }
}
