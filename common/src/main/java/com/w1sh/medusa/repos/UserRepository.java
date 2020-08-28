package com.w1sh.medusa.repos;

import com.mongodb.client.result.DeleteResult;
import com.w1sh.medusa.data.User;
import com.w1sh.medusa.utils.Reactive;
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

@Repository
public class UserRepository {

    private final ReactiveMongoTemplate template;

    public UserRepository(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.template = reactiveMongoTemplate;
    }

    public Mono<User> save(User user) {
        final Query query = new Query(Criteria.where("userId").is(user.getUserId()))
                .addCriteria(Criteria.where("guildId").is(user.getGuildId()));
        return template.exists(query, User.class)
                .transform(Reactive.ifElse(bool -> update(query, user), bool -> template.save(user)));
    }

    public Mono<User> update(Query query, User user) {
        final Update update = new Update().set("points", user.getPoints());
        final FindAndModifyOptions modifyOptions = FindAndModifyOptions.options().returnNew(true);
        return template.findAndModify(query, update, modifyOptions, User.class);
    }

    public Mono<List<User>> findAllByGuildId(String guildId) {
        final Query query = new Query(Criteria.where("guildId").is(guildId));
        return template.find(query, User.class).collectList();
    }

    public Mono<DeleteResult> remove(User user) {
        return template.remove(user);
    }

    public Flux<User> findTop5ByGuildIdOrderByPoints(String guildId) {
        final Query query = new Query(Criteria.where("guildId").is(guildId))
                .limit(5)
                .with(Sort.by(Sort.Direction.DESC, "points"));
        return template.find(query, User.class);
    }
}
