package com.w1sh.medusa.repos;

import com.w1sh.medusa.data.ChannelRule;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ChannelRuleRepository extends ReactiveCrudRepository<ChannelRule, Integer> {

    @Query(value = "SELECT * FROM core.channels_rules WHERE channel_id = :channelId")
    Flux<ChannelRule> findAllByChannel(String channelId);
}
