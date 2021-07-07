package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.Wishlist;
import com.w1sh.medusa.events.WishlistEvent;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.services.WishlistService;
import discord4j.core.spec.EmbedCreateSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Component
public final class WishlistEventListener implements CustomEventListener<WishlistEvent> {

    private static final Logger log = LoggerFactory.getLogger(WishlistEventListener.class);
    private final WishlistService wishlistService;
    private final MessageService messageService;

    public WishlistEventListener(WishlistService wishlistService, MessageService messageService) {
        this.wishlistService = wishlistService;
        this.messageService = messageService;
    }

    @Override
    public Mono<Void> execute(WishlistEvent event) {
        return wishlistService.findByUserId(event.getUserId())
                .flatMap(playlists -> messageService.send(event.getChannel(), createWishlistEmbedSpec(playlists, event)))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> log.error("Failed to list all playlists of user", throwable)))
                .then();
    }

    private Consumer<EmbedCreateSpec> createWishlistEmbedSpec(Wishlist wishlist, WishlistEvent event) {
        return embedCreateSpec -> {};
    }
}
