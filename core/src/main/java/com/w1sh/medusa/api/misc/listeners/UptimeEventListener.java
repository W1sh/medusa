package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.UptimeEvent;
import com.w1sh.medusa.data.events.EventFactory;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.metrics.Trackers;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class UptimeEventListener implements EventListener<UptimeEvent> {

    private final ResponseDispatcher responseDispatcher;

    public UptimeEventListener(ResponseDispatcher responseDispatcher) {
        this.responseDispatcher = responseDispatcher;
        EventFactory.registerEvent(UptimeEvent.KEYWORD, UptimeEvent.class);
    }

    @Override
    public Class<UptimeEvent> getEventType() {
        return UptimeEvent.class;
    }

    @Override
    public Mono<Void> execute(UptimeEvent event) {
        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan,
                        String.format("Medusa has been online for %s", Trackers.getUptime()), false))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }
}
