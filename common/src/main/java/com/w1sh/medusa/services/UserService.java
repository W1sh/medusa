package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.data.User;
import com.w1sh.medusa.repos.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.cache.CacheMono;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.time.Duration;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final UserRepository repository;

    private final Cache<String, User> cache;

    public UserService(UserRepository repository) {
        this.repository = repository;
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(6))
                .build();
    }

    public Mono<User> save(User user){
        return repository.save(user)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to save user with id \"{}\"", user.getId(), t)))
                .doOnNext(u -> cache.put(u.getUserId(), u));
    }

    public Mono<User> findById(Integer id) {
        return repository.findById(id)
                .doOnNext(user -> cache.put(user.getUserId(), user))
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to retrieve user with id \"{}\"", id, t)));
    }

    public Mono<User> findByUserId(String userId) {
        return CacheMono.lookup(key -> Mono.justOrEmpty(cache.getIfPresent(key))
                .map(Signal::next), userId)
                .onCacheMissResume(() -> repository.findByUserId(userId).switchIfEmpty(save(new User(userId))))
                .andWriteWith((key, signal) -> Mono.fromRunnable(() -> Optional.ofNullable(signal.get()).ifPresent(value -> cache.put(key, value))))
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to retrieve user with user id \"{}\"", userId, t)));
    }
}
