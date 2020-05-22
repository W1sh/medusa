package com.w1sh.medusa.repos;

import com.w1sh.medusa.data.PointDistribution;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PointDistributionRepository extends ReactiveCrudRepository<PointDistribution, Integer> {
}
