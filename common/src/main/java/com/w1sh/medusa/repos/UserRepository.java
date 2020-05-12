package com.w1sh.medusa.repos;

import com.w1sh.medusa.data.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {

    @Query(value = "SELECT * FROM core.users WHERE user_id = :userId")
    Mono<User> findByUserId(Long userId);

    @Query(value = "SELECT * FROM core.users ORDER BY points DESC")
    Flux<User> findAllOrderByPoints();
}
