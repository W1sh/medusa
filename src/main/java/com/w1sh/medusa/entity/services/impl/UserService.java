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
        return Mono.just(user)
                .filterWhen(u -> userRepository.isPresent(u)
                        .hasElement()
                        .map(b -> !b))
                .doOnNext(u -> userRepository.persist(user))
                .then();
    }

    @Override
    public Flux<User> read() {
        return userRepository.read();
    }

    @Override
    public Mono<Integer> update(final User user) {
        return Mono.just(user)
                .doOnNext(u -> u.setPoints(u.getPoints() + 100))
                .flatMap(u -> userRepository.update(u))
                .doOnError(error -> System.out.println(error.getLocalizedMessage()));
    }
}
