package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.events.WhenPointsEvent;
import com.w1sh.medusa.core.Executor;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.listeners.CustomEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class WhenPointsEventListener implements CustomEventListener<WhenPointsEvent> {

    private final Executor executor;
    private final MessageService messageService;

    @Override
    public Mono<Void> execute(WhenPointsEvent event) {
        return event.getChannel()
                .map(chan -> new TextMessage(chan, String.format("**%s minutes** left until next point distribution!", executor.getNextDistribution().toMinutes()), false))
                .doOnNext(messageService::queue)
                .doAfterTerminate(messageService::flush)
                .then();
    }
}
