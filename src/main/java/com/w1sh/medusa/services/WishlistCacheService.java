package com.w1sh.medusa.services;

import com.w1sh.medusa.data.Wishlist;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public interface WishlistCacheService {

    void put(String userId, Wishlist wishlist);

    Mono<Wishlist> findByUserId(String userId, Supplier<Mono<Wishlist>> onCacheMissSupplier);
}
