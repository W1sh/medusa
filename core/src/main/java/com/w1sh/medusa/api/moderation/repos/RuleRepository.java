package com.w1sh.medusa.api.moderation.repos;

import com.w1sh.medusa.api.moderation.data.Rule;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RuleRepository extends ReactiveCrudRepository<Rule, Integer> {
}
