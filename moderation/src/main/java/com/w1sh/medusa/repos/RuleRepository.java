package com.w1sh.medusa.repos;

import com.w1sh.medusa.data.Rule;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RuleRepository extends ReactiveCrudRepository<Rule, Integer> {
}
