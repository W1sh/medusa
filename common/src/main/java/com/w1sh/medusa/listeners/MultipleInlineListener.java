package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.events.MultipleInlineEvent;
import com.w1sh.medusa.dispatchers.MedusaEventDispatcher;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class MultipleInlineListener implements EventListener<MultipleInlineEvent> {

    private final MedusaEventDispatcher medusaEventDispatcher;
    private final ResponseDispatcher responseDispatcher;

    @Override
    public Class<MultipleInlineEvent> getEventType() {
        return MultipleInlineEvent.class;
    }

    @Override
    public Mono<Void> execute(MultipleInlineEvent event) {
        return Mono.just(event)
                .flatMapIterable(MultipleInlineEvent::getEvents)
                .doOnNext(medusaEventDispatcher::publish)
                .count()
                .doOnNext(responseDispatcher::flush)
                .then();
    }
}
