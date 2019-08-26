package com.w1sh.medusa.entity.services;

import com.w1sh.medusa.entity.entities.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IUserService {

    Mono<Void> persist(User user);

    Flux<User> read();

    Mono<Void> update(User user);
}
