package com.w1sh.medusa.api.moderation.listeners;

import com.w1sh.medusa.api.moderation.events.ChannelRulesEvent;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class ChannelRulesEventListener implements EventListener<ChannelRulesEvent> {

    private final ChannelRulesShowAction channelRulesShowAction;
    private final ChannelRulesActivateAction channelRulesActivateAction;
    private final ChannelRulesDeactivateAction channelRulesDeactivateAction;
    private final ResponseDispatcher responseDispatcher;

    @Override
    public Class<ChannelRulesEvent> getEventType() {
        return ChannelRulesEvent.class;
    }

    @Override
    public Mono<Void> execute(ChannelRulesEvent event) {
        return applyAction(event)
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<? extends Response> applyAction(ChannelRulesEvent event) {
        if(event.getArguments().isEmpty()) return channelRulesShowAction.apply(event);
        String action = event.getArguments().get(1);
        if ("on".equals(action)) {
            return channelRulesActivateAction.apply(event);
        } else if ("off".equals(action)) {
            return channelRulesDeactivateAction.apply(event);
        }
        return Mono.empty();
    }
}
