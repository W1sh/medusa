package com.w1sh.medusa.core.listeners;

import com.w1sh.medusa.listeners.CardSearchEventListener;
import com.w1sh.medusa.listeners.DiscordEventListener;
import discord4j.core.event.domain.message.ReactionAddEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class ReactionAddEventListener implements DiscordEventListener<ReactionAddEvent> {

    private final CardSearchEventListener cardSearchEventListener;

    @Override
    public Mono<Void> execute(ReactionAddEvent event) {
        return cardSearchEventListener.update(event);
    }
}
