package com.w1sh.medusa.repos;

import com.w1sh.medusa.data.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {
}
