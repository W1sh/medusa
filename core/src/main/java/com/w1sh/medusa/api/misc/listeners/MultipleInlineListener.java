package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.MultipleInlineEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MultipleInlineListener implements EventListener<MultipleInlineEvent> {

    private final CommandEventDispatcher commandEventDispatcher;
    private final ResponseDispatcher responseDispatcher;

    public MultipleInlineListener(CommandEventDispatcher eventDispatcher, ResponseDispatcher responseDispatcher) {
        commandEventDispatcher = eventDispatcher;
        this.responseDispatcher = responseDispatcher;
        EventFactory.registerEvent(MultipleInlineEvent.KEYWORD, MultipleInlineEvent.class);
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<MultipleInlineEvent> getEventType() {
        return MultipleInlineEvent.class;
    }

    @Override
    public Mono<Void> execute(MultipleInlineEvent event) {
        return Mono.just(event)
                .flatMapIterable(MultipleInlineEvent::getEvents)
                .doOnNext(commandEventDispatcher::publish)
                .count()
                .doOnNext(responseDispatcher::flush)
                .then();
    }
}
