package com.w1sh.medusa.entity.services.impl;

import com.w1sh.medusa.entity.entities.User;
import com.w1sh.medusa.entity.repositories.IUserRepository;
import com.w1sh.medusa.entity.services.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService implements IUserService {

    private final IUserRepository userRepository;

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

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
