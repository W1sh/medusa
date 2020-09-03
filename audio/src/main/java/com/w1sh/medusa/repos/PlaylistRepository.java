package com.w1sh.medusa.repos;

import com.mongodb.client.result.DeleteResult;
import com.w1sh.medusa.data.Playlist;
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

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PlaylistRepository {

    private final ReactiveMongoTemplate template;

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

    public Mono<DeleteResult> removeByUserId(String userId) {
        final Query query = new Query(Criteria.where("userId").is(userId));
        return template.remove(query, Playlist.class)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete playlist from user with id \"{}\"", userId, t)));
    }

    public Mono<DeleteResult> remove(Playlist playlist) {
        return template.remove(playlist)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete playlist with id \"{}\"", playlist.getId(), t)));
    }

    public Mono<List<Playlist>> findAllByUserId(String userId){
        final Query query = new Query(Criteria.where("userId").is(userId));
        return template.find(query, Playlist.class).collectList();
    }
}
