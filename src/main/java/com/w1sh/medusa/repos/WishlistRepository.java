package com.w1sh.medusa.repos;

import com.w1sh.medusa.data.Wishlist;
import reactor.core.publisher.Mono;

public interface WishlistRepository {

    Mono<Wishlist> findByUserId(String userId);

    Mono<Wishlist> save(Wishlist wishlist);
}
