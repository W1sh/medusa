package com.w1sh.medusa.repos;

import com.mongodb.client.result.DeleteResult;
import com.w1sh.medusa.data.Channel;
import com.w1sh.medusa.utils.Reactive;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class ChannelRepository {

    private final ReactiveMongoTemplate template;

    public ChannelRepository(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.template = reactiveMongoTemplate;
    }

    public Mono<Channel> save(Channel channel) {
        final Query query = new Query(Criteria.where("channelId").is(channel.getChannelId()));
        return template.exists(query, Channel.class)
                .transform(Reactive.ifElse(bool -> update(query, channel), bool -> template.save(channel)));
    }

    public Mono<Channel> update(Query query, Channel channel) {
        final Update update = new Update().set("rules", channel.getRules());
        final FindAndModifyOptions modifyOptions = FindAndModifyOptions.options().returnNew(true);
        return template.findAndModify(query, update, modifyOptions, Channel.class);
    }

    public Mono<DeleteResult> remove(Channel channel) {
        return template.remove(channel);
    }

    public Mono<Channel> findByChannel(String channel) {
        return template.findOne(Query.query(Criteria.where("channelId").is(channel)), Channel.class);
    }

}
