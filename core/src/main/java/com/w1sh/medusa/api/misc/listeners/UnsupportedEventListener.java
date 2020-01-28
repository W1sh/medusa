package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.UnsupportedEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.utils.Messenger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class UnsupportedEventListener implements EventListener<UnsupportedEvent> {

    @Value("${event.unsupported}")
    private String unsupported;

    public UnsupportedEventListener(CommandEventDispatcher eventDispatcher) {
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<UnsupportedEvent> getEventType() {
        return UnsupportedEvent.class;
    }

    @Override
    public Mono<Void> execute(UnsupportedEvent event) {
        return event.getMessage().getChannel()
                .doOnNext(channel -> Messenger.send(event, unsupported).subscribe())
                .then();
    }
}
