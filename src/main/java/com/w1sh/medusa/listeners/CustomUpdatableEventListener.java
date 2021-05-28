package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.Event;
import discord4j.core.event.domain.message.ReactionAddEvent;
import reactor.core.publisher.Mono;

public interface CustomUpdatableEventListener<T extends Event> extends CustomEventListener<T>{

    Mono<Void> update(ReactionAddEvent event);
}
