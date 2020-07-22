package com.w1sh.medusa.repos;

import com.w1sh.medusa.data.Warning;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface WarningRepository extends ReactiveCrudRepository<Warning, Integer> {

    @Query(value = "SELECT * FROM core.warnings WHERE fk_user = :userId")
    Flux<Warning> findAllByUser(Integer userId);
}
