package com.w1sh.medusa.services;

import com.w1sh.medusa.data.Wishlist;
import com.w1sh.medusa.repos.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public final class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistCacheService wishlistCacheService;

    public Mono<Wishlist> findByUserId(String userId){
        return wishlistCacheService.findByUserId(userId, () -> wishlistRepository.findByUserId(userId))
                .defaultIfEmpty(new Wishlist(userId));
    }

    public Mono<Wishlist> save(Wishlist wishlist) {
        return wishlistRepository.save(wishlist)
                .doOnNext(w -> wishlistCacheService.put(w.getUserId(), w));
    }
}
