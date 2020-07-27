package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.events.WhenPointsEvent;
import com.w1sh.medusa.core.Executor;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class WhenPointsEventListener implements EventListener<WhenPointsEvent> {

    private final Executor executor;
    private final ResponseDispatcher responseDispatcher;

    @Override
    public Class<WhenPointsEvent> getEventType() {
        return WhenPointsEvent.class;
    }

    @Override
    public Mono<Void> execute(WhenPointsEvent event) {
        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, String.format("**%s minutes** left until next point distribution!", executor.getNextRun().toMinutes()), false))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }
}
