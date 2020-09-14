package com.w1sh.medusa.repos;

import com.mongodb.client.result.DeleteResult;
import com.w1sh.medusa.data.User;
import com.w1sh.medusa.utils.Reactive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepository {

    private static final String POINTS_FIELD = "points";
    private static final String USER_ID_FIELD = "userId";
    private static final String GUILD_ID_FIELD = "guildId";

    private final ReactiveMongoTemplate template;

    public Mono<User> save(User user) {
        final Query query = new Query(Criteria.where(USER_ID_FIELD).is(user.getUserId()))
                .addCriteria(Criteria.where(GUILD_ID_FIELD).is(user.getGuildId()));
        return template.exists(query, User.class)
                .transform(Reactive.ifElse(bool -> update(query, user), bool -> template.save(user)));
    }

    public Mono<User> update(Query query, User user) {
        final Update update = new Update().set(POINTS_FIELD, user.getPoints());
        final FindAndModifyOptions modifyOptions = FindAndModifyOptions.options().returnNew(true);
        return template.findAndModify(query, update, modifyOptions, User.class);
    }

    public Mono<List<User>> findAllByGuildId(String guildId) {
        final Query query = new Query(Criteria.where(GUILD_ID_FIELD).is(guildId));
        return template.find(query, User.class).collectList();
    }

    public Mono<DeleteResult> removeByUserId(String userId) {
        final Query query = new Query(Criteria.where(USER_ID_FIELD).is(userId));
        return template.remove(query, User.class)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete user with id \"{}\"", userId, t)));
    }

    public Mono<DeleteResult> removeByGuildId(String guildId) {
        final Query query = new Query(Criteria.where(GUILD_ID_FIELD).is(guildId));
        return template.remove(query, User.class)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete all users in guild with id \"{}\"", guildId, t)));
    }

    public Mono<DeleteResult> remove(User user) {
        return template.remove(user)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete user with id \"{}\"", user.getUserId(), t)));
    }

    public Flux<User> findTop5ByGuildIdOrderByPoints(String guildId) {
        final Query query = new Query(Criteria.where("guildId").is(guildId))
                .limit(5)
                .with(Sort.by(Sort.Direction.DESC, POINTS_FIELD));
        return template.find(query, User.class);
    }
}
