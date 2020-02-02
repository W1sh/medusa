package com.w1sh.medusa.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.data.User;
import com.w1sh.medusa.repos.UserRepository;
import discord4j.core.object.entity.Member;
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

    public Mono<User> findByUserId(Long userId) {
        return userRepository.findByUserId(String.valueOf(userId))
                .defaultIfEmpty(new User(userId));
    }

    public Mono<Void> distributePoints(Member member) {
        return findByUserId(member.getId().asLong())
                .doOnNext(user -> user.setPoints(user.getPoints() + 100))
                .flatMap(this::save)
                .then();
    }

}
