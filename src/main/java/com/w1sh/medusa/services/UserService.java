package com.w1sh.medusa.services;

import com.w1sh.medusa.model.entities.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<Void> persist(User user);

    Flux<User> read();

    Mono<Void> update(User user);
}
