package com.w1sh.medusa.repos;

import com.w1sh.medusa.data.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {

    @Query(value = "SELECT * FROM core.users WHERE user_id = :userId")
    Mono<User> findByUserId(String userId);
}
