package com.w1sh.medusa.services.impl;

import com.w1sh.medusa.model.entities.User;
import com.w1sh.medusa.repositories.UserRepository;
import com.w1sh.medusa.services.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Mono<Void> persist(final User user) {
        return Mono.just(user)
                .filterWhen(u -> userRepository.isPresent(u)
                        .hasElement()
                        .map(b -> !b))
                //.doOnNext(u -> userRepository.persist(user))
                .then();
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<User> read() {
        return userRepository.read();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Mono<Void> update(final User user) {
        return Mono.just(user)
                .doOnNext(u -> u.setPoints(u.getPoints() + 100))
                //.doOnNext(userRepository::update)
                .doOnError(error -> System.out.println(error.getLocalizedMessage()))
                .then();
    }
}
