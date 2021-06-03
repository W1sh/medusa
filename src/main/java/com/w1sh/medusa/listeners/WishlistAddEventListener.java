package com.w1sh.medusa.listeners;

import com.w1sh.medusa.events.WishlistAddEvent;
import com.w1sh.medusa.services.CardService;
import com.w1sh.medusa.services.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Slf4j
@Component
@RequiredArgsConstructor
public final class WishlistAddEventListener implements CustomEventListener<WishlistAddEvent>{

    private final WishlistService wishlistService;
    private final CardService cardService;

    @Override
    public Mono<Void> execute(WishlistAddEvent event) {
        String cardName = String.join(" ", event.getArguments());

        return cardService.getCardByName(cardName)
                .zipWith(wishlistService.findByUserId(event.getUserId()))
                .doOnNext(objects -> objects.getT2().getCards().add(objects.getT1().getId()))
                .map(Tuple2::getT2)
                .flatMap(wishlistService::save)
                .then();
    }

}
