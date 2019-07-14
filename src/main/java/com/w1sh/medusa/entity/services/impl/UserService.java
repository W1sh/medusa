package com.w1sh.medusa.entity.services.impl;

import com.w1sh.medusa.entity.entities.User;
import com.w1sh.medusa.entity.repositories.IUserRepository;
import com.w1sh.medusa.entity.repositories.impl.UserRepository;
import com.w1sh.medusa.entity.services.IUserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class UserService implements IUserService {

    private IUserRepository userRepository = new UserRepository();

    @Override
    public Mono<Void> persist(final User user) {
        System.out.println("Persisting!");
        return Mono.just(user)
                //.filter(u -> !userRepository.isPresent(u))
                .map(tuple2 -> {
                    userRepository.persist(user);
                    return tuple2;
                }).then();
    }

    @Override
    public Flux<User> read() {
        return null;
    }

    @Override
    public Mono<Integer> update(final User user) {
        return Mono.just(user)
                .doOnNext(u -> u.setPoints(u.getPoints() + 100))
                .flatMap(u -> userRepository.update(u))
                .doOnError(error -> System.out.println(error.getLocalizedMessage()));
    }
}
