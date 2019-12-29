package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.MultipleInlineEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MultipleInlineListener implements EventListener<MultipleInlineEvent> {

    private final CommandEventDispatcher commandEventDispatcher;

    public MultipleInlineListener(CommandEventDispatcher eventDispatcher) {
        commandEventDispatcher = eventDispatcher;
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
                //.take(1)
                //.doOnNext(commandEventDispatcher::publish)
                //.then()
                .doOnNext(commandEventDispatcher::publish)
                .doOnEach(signal -> System.out.println(signal.getType().toString()))
                .then();
    }
}
