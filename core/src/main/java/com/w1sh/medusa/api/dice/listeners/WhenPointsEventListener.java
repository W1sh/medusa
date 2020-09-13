package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.events.WhenPointsEvent;
import com.w1sh.medusa.core.Executor;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.listeners.CustomEventListener;
import com.w1sh.medusa.services.MessageService;
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
        return messageService.send(event.getChannel(), MessageEnum.WHENPOINTS, String.valueOf(executor.getNextDistribution().toMinutes()))
                .then();
    }
}
