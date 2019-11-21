package com.w1sh.medusa.core.listeners;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public interface PermissionsEventListener<T extends Event> {

    Mono<Boolean> hasPermissions(T event);
}
