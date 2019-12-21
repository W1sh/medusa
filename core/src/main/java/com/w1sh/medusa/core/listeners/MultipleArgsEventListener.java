package com.w1sh.medusa.core.listeners;

import com.w1sh.medusa.core.events.MultipleArgumentsEvent;
import reactor.core.publisher.Mono;

public interface MultipleArgsEventListener<T extends MultipleArgumentsEvent> extends EventListener<T> {

    Mono<Boolean> validate(T event);
}
