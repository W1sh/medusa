package com.w1sh.medusa.repos;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoClients;
import com.w1sh.medusa.data.ChannelRule;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class ChannelRuleRepository {

    private final ReactiveMongoTemplate template;

    public ChannelRuleRepository() {
        this.template = new ReactiveMongoTemplate(MongoClients.create(), "test");
    }

    public Mono<ChannelRule> save(ChannelRule channelRule) {
        return template.save(channelRule);
    }

    public Mono<DeleteResult> remove(ChannelRule channelRule) {
        return template.remove(channelRule);
    }

    public Flux<ChannelRule> findAllByChannel(String channel) {
        return template.find(Query.query(Criteria.where("channel").is(channel)), ChannelRule.class);
    }

}
