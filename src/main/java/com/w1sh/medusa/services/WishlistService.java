package com.w1sh.medusa.services;

import com.w1sh.medusa.data.Wishlist;
import com.w1sh.medusa.repos.WishlistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class WishlistService {

    private static final Logger log = LoggerFactory.getLogger(WishlistService.class);
    private final WishlistRepository wishlistRepository;
    private final WishlistCacheService wishlistCacheService;

    public WishlistService(WishlistRepository wishlistRepository, WishlistCacheService wishlistCacheService) {
        this.wishlistRepository = wishlistRepository;
        this.wishlistCacheService = wishlistCacheService;
    }

    public Mono<Wishlist> findByUserId(String userId){
        return wishlistCacheService.findByUserId(userId, () -> wishlistRepository.findByUserId(userId))
                .defaultIfEmpty(new Wishlist(userId));
    }

    public Mono<Wishlist> save(Wishlist wishlist) {
        return wishlistRepository.save(wishlist)
                .doOnNext(w -> wishlistCacheService.put(w.getUserId(), w));
    }
}
