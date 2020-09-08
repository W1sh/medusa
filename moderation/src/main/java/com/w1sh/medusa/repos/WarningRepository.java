package com.w1sh.medusa.repos;

import com.mongodb.client.result.DeleteResult;
import com.w1sh.medusa.data.Warning;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class WarningRepository {

    private static final String CHANNEL_ID_FIELD = "channelId";
    private static final String USER_ID_FIELD = "userId";
    private static final String GUILD_ID_FIELD = "guildId";

    private final ReactiveMongoTemplate template;

    public Mono<Warning> save(Warning warning){
        return template.save(warning);
    }

    public Mono<DeleteResult> removeByUserIdAndGuildId(String userId, String guildId) {
        final Query query = new Query(Criteria.where(USER_ID_FIELD).is(userId))
                .addCriteria(Criteria.where(GUILD_ID_FIELD).is(guildId));
        return remove(query);
    }

    public Mono<DeleteResult> removeByChannelId(String channelId) {
        final Query query = new Query(Criteria.where(CHANNEL_ID_FIELD).is(channelId));
        return remove(query);
    }

    public Mono<DeleteResult> removeByUserId(String userId) {
        final Query query = new Query(Criteria.where(USER_ID_FIELD).is(userId));
        return remove(query);
    }

    public Mono<DeleteResult> removeByGuildId(String guildId) {
        final Query query = new Query(Criteria.where(GUILD_ID_FIELD).is(guildId));
        return remove(query);
    }

    private Mono<DeleteResult> remove(Query query) {
        return template.remove(query, Warning.class)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete warning", t)));
    }
}
