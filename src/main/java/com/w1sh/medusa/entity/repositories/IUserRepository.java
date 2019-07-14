package com.w1sh.medusa.entity.repositories;

import com.w1sh.medusa.entity.entities.User;
import reactor.core.publisher.Mono;

public interface IUserRepository extends IRepository<User, Long> {

    Mono<Long> isPresent(User user);
}
