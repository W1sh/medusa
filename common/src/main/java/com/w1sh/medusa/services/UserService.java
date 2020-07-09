package com.w1sh.medusa.services;

import com.w1sh.medusa.data.User;
import com.w1sh.medusa.repos.UserRepository;
import com.w1sh.medusa.services.cache.MemoryCache;
import com.w1sh.medusa.services.cache.MemoryCacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@Slf4j
public class UserService {

    private final UserRepository repository;
    private final MemoryCache<String, User> cache;

    public UserService(UserRepository repository) {
        this.repository = repository;
        this.cache = new MemoryCacheBuilder<String, User>()
                .expireAfterAccess(Duration.ofHours(6))
                .fetch(key -> repository.findByUserId(key).switchIfEmpty(save(new User(key))))
                .build();
    }

    @Transactional
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
        return cache.get(userId)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to retrieve user with user id \"{}\"", userId, t)));
    }
}
