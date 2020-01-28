package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.UptimeEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.metrics.Trackers;
import com.w1sh.medusa.utils.Messenger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class UptimeEventListener implements EventListener<UptimeEvent> {

    public UptimeEventListener(CommandEventDispatcher eventDispatcher) {
        EventFactory.registerEvent(UptimeEvent.KEYWORD, UptimeEvent.class);
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<UptimeEvent> getEventType() {
        return UptimeEvent.class;
    }

    @Override
    public Mono<Void> execute(UptimeEvent event) {
        return Mono.just(event)
                .doOnNext(ev -> Messenger.send(ev, String.format("Medusa has been online for %s", Trackers.getUptime())).subscribe())
                .then();
    }
}
