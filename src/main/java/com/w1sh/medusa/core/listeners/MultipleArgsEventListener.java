package com.w1sh.medusa.core.listeners;

import com.w1sh.medusa.api.CommandEvent;
import reactor.core.publisher.Mono;

public interface MultipleArgsEventListener<T extends CommandEvent> extends EventListener<T> {

    Mono<Boolean> validate(T event);
}
