package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.events.UnsupportedEvent;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class UnsupportedEventListener implements EventListener<UnsupportedEvent> {

    private final ResponseDispatcher responseDispatcher;

    @Value("${event.unsupported}")
    private String unsupported;

    public UnsupportedEventListener(CommandEventDispatcher eventDispatcher, ResponseDispatcher responseDispatcher) {
        this.responseDispatcher = responseDispatcher;
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<UnsupportedEvent> getEventType() {
        return UnsupportedEvent.class;
    }

    @Override
    public Mono<Void> execute(UnsupportedEvent event) {
        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, unsupported, false))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }
}
