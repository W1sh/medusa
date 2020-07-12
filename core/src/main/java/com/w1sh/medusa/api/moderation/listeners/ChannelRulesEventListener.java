package com.w1sh.medusa.api.moderation.listeners;

import com.w1sh.medusa.api.moderation.events.ChannelRulesEvent;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class ChannelRulesEventListener implements EventListener<ChannelRulesEvent> {

    private final ChannelRulesShowAction channelRulesShowAction;
    private final ResponseDispatcher responseDispatcher;

    @Override
    public Class<ChannelRulesEvent> getEventType() {
        return ChannelRulesEvent.class;
    }

    @Override
    public Mono<Void> execute(ChannelRulesEvent event) {
        return channelRulesShowAction.apply(event)
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }
}
