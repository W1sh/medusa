package com.w1sh.medusa.listeners;

import com.w1sh.medusa.events.WishlistRemoveEvent;
import com.w1sh.medusa.services.CardService;
import com.w1sh.medusa.services.WishlistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Component
public final class WishlistRemoveEventListener implements CustomEventListener<WishlistRemoveEvent>{

    private static final Logger log = LoggerFactory.getLogger(WishlistRemoveEventListener.class);
    private final WishlistService wishlistService;
    private final CardService cardService;

    public WishlistRemoveEventListener(WishlistService wishlistService, CardService cardService) {
        this.wishlistService = wishlistService;
        this.cardService = cardService;
    }

    @Override
    public Mono<Void> execute(WishlistRemoveEvent event) {
        String cardName = String.join(" ", event.getArguments());

        return cardService.getCardByName(cardName)
                .zipWith(wishlistService.findByUserId(event.getUserId()))
                .doOnNext(objects -> objects.getT2().getCards().remove(objects.getT1().getId()))
                .map(Tuple2::getT2)
                .flatMap(wishlistService::save)
                .then();
    }

}
