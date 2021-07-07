package com.w1sh.medusa.core.listeners;

import com.w1sh.medusa.listeners.CardSearchEventListener;
import com.w1sh.medusa.listeners.DiscordEventListener;
import discord4j.core.event.domain.message.ReactionAddEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class ReactionAddEventListener implements DiscordEventListener<ReactionAddEvent> {

    private final CardSearchEventListener cardSearchEventListener;

    public ReactionAddEventListener(CardSearchEventListener cardSearchEventListener) {
        this.cardSearchEventListener = cardSearchEventListener;
    }

    @Override
    public Mono<Void> execute(ReactionAddEvent event) {
        return cardSearchEventListener.update(event);
    }
}
