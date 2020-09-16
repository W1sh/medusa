package com.w1sh.medusa.repos;

import com.mongodb.client.result.DeleteResult;
import com.w1sh.medusa.data.Channel;
import com.w1sh.medusa.data.Rule;
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

    private static final String CHANNEL_ID_FIELD = "channelId";
    private static final String BLOCKLIST_FIELD = "blocklist";
    private static final String RULES_FIELD = "rules";
    private static final String GUILD_ID_FIELD = "guildId";

    private final ReactiveMongoTemplate template;

    public Mono<Channel> save(Channel channel) {
        final Query query = new Query(Criteria.where(CHANNEL_ID_FIELD).is(channel.getChannelId()));
        return template.exists(query, Channel.class)
                .transform(Reactive.ifElse(bool -> update(query, channel), bool -> template.save(channel)));
    }

    public Mono<Channel> update(Query query, Channel channel) {
        final Update update = new Update().set(RULES_FIELD, channel.getRules())
                .set(BLOCKLIST_FIELD, channel.getBlocklist());
        final FindAndModifyOptions modifyOptions = FindAndModifyOptions.options().returnNew(true);
        return template.findAndModify(query, update, modifyOptions, Channel.class);
    }

    public Mono<DeleteResult> removeByChannelId(String channelId) {
        final Query query = new Query(Criteria.where(CHANNEL_ID_FIELD).is(channelId));
        return template.remove(query, Channel.class)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete channel with id \"{}\"", channelId, t)));
    }

    public Mono<DeleteResult> removeByGuildId(String guildId) {
        final Query query = new Query(Criteria.where(GUILD_ID_FIELD).is(guildId));
        return template.remove(query, Channel.class)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete all channels from guild with id \"{}\"", guildId, t)));
    }

    public Mono<Boolean> containsRule(String channelId, Rule rule) {
        final Query query = new Query(Criteria.where(CHANNEL_ID_FIELD).is(channelId))
                .addCriteria(Criteria.where(RULES_FIELD).all(rule))
                .limit(1);
        return template.exists(query, Channel.class)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to check if channel with id \"{}\" exists", channelId, t)));
    }

    public Mono<DeleteResult> remove(Channel channel) {
        return template.remove(channel)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete channel with id \"{}\"", channel.getChannelId(), t)));
    }

    public Mono<Channel> findByChannel(String channel) {
        return template.findOne(Query.query(Criteria.where(CHANNEL_ID_FIELD).is(channel)), Channel.class);
    }
}
