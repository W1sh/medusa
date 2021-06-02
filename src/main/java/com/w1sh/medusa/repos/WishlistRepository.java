package com.w1sh.medusa.repos;

import com.w1sh.medusa.data.Wishlist;
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
public class WishlistRepository {

    private final ReactiveMongoTemplate template;

    public Mono<Wishlist> findByUserId(String userId){
        final Query query = new Query(Criteria.where("userId").is(userId));
        return template.findOne(query, Wishlist.class);
    }
}
