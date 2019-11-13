package com.w1sh.medusa.repositories;

import com.w1sh.medusa.model.entities.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository {

    Flux<User> read();

    Mono<User> read(Long id);

    void persist(User entity);

    void update(User entity);

    void delete(User entity);

    Mono<Long> isPresent(User user);
}
