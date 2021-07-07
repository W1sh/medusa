package com.w1sh.medusa.repos;

import com.w1sh.medusa.data.Wishlist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class WishlistMongoRepository implements WishlistRepository {

    private static final Logger log = LoggerFactory.getLogger(WishlistMongoRepository.class);
    private final ReactiveMongoTemplate template;

    public WishlistMongoRepository(ReactiveMongoTemplate template) {
        this.template = template;
    }

    public Mono<Wishlist> findByUserId(String userId){
        final Query query = new Query(Criteria.where("userId").is(userId));
        return template.findOne(query, Wishlist.class);
    }

    public Mono<Wishlist> save(Wishlist wishlist) {
        return template.save(wishlist);
    }

}
