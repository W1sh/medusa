package com.w1sh.medusa.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.data.User;
import com.w1sh.medusa.repos.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final Cache<Long, User> usersCache;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.usersCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterAccess(Duration.ofHours(1))
                .recordStats()
                .build();
    }

    public Mono<User> save(User user){
        return userRepository.save(user);
    }

    public Mono<User> findById(Long userId) {
        return userRepository.findById(userId)
                .defaultIfEmpty(new User(userId));
    }

}
