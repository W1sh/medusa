package com.w1sh.medusa.core.listeners;

import com.w1sh.medusa.listeners.DiscordEventListener;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public final class DisconnectEventListener implements DiscordEventListener<DisconnectEvent> {

    @Override
    public Mono<Void> execute(DisconnectEvent event) {
        if (event.getCause().isPresent()) {
            log.error("Disconnected from gateway with status {}", event.getStatus(), event.getCause().get());
        } else {
            log.error("Disconnected from gateway with status {}", event.getStatus());
        }
        return Mono.empty();
    }
}
