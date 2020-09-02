package com.w1sh.medusa.repos;

import com.mongodb.client.result.DeleteResult;
import com.w1sh.medusa.data.Channel;
import com.w1sh.medusa.utils.Reactive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ChannelRepository {

    private final ReactiveMongoTemplate template;

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

    public Mono<DeleteResult> removeByChannelId(String channelId) {
        final Query query = new Query(Criteria.where("channelId").is(channelId));
        return template.remove(query, Channel.class)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete channel with id \"{}\"", channelId, t)));
    }

    public Mono<DeleteResult> removeByGuildId(String guildId) {
        final Query query = new Query(Criteria.where("guildId").is(guildId));
        return template.remove(query, Channel.class)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete all channels from guild with id \"{}\"", guildId, t)));
    }

    public Mono<DeleteResult> remove(Channel channel) {
        return template.remove(channel)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete channel with id \"{}\"", channel.getChannelId(), t)));
    }

    public Mono<Channel> findByChannel(String channel) {
        return template.findOne(Query.query(Criteria.where("channelId").is(channel)), Channel.class);
    }
}
