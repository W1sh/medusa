package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.UptimeEvent;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.CustomEventListener;
import com.w1sh.medusa.metrics.Trackers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class UptimeEventListener implements CustomEventListener<UptimeEvent> {

    private final ResponseDispatcher responseDispatcher;

    @Override
    public Mono<Void> execute(UptimeEvent event) {
        return event.getChannel()
                .map(chan -> new TextMessage(chan,
                        String.format("Medusa has been online for %s", Trackers.getUptime()), false))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }
}
