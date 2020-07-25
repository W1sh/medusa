package com.w1sh.medusa.repos;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoClients;
import com.w1sh.medusa.data.Playlist;
import com.w1sh.medusa.utils.Reactive;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public class PlaylistRepository {

    private final ReactiveMongoTemplate template;

    public PlaylistRepository() {
        this.template = new ReactiveMongoTemplate(MongoClients.create(), "test");
    }

    public Mono<Playlist> save(Playlist playlist) {
        final Query query = new Query(Criteria.where("userId").is(playlist.getUserId()));
        return template.exists(query, Playlist.class)
                .transform(Reactive.ifElse(bool -> update(query, playlist), bool -> template.save(playlist)));
    }

    public Mono<Playlist> update(Query query, Playlist playlist) {
        final Update update = new Update().set("tracks", playlist.getTracks());
        final FindAndModifyOptions modifyOptions = FindAndModifyOptions.options().returnNew(true);
        return template.findAndModify(query, update, modifyOptions, Playlist.class);
    }

    public Mono<Boolean> delete(Playlist playlist) {
        return template.remove(playlist).map(DeleteResult::wasAcknowledged);
    }

    public Mono<List<Playlist>> findAllByUserId(String userId){
        final Query query = new Query(Criteria.where("userId").is(userId));
        return template.find(query, Playlist.class).collectList();
    }
}
