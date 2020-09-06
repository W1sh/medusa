package com.w1sh.medusa.listeners;

import com.w1sh.medusa.actions.ChannelRulesActivateAction;
import com.w1sh.medusa.actions.ChannelRulesDeactivateAction;
import com.w1sh.medusa.actions.ChannelRulesShowAction;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.ChannelRulesEvent;
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
    public Mono<Void> execute(ChannelRulesEvent event) {
        return applyAction(event)
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<? extends Response> applyAction(ChannelRulesEvent event) {
        if(event.getArguments().isEmpty()) return channelRulesShowAction.apply(event);
        if(event.getArguments().size() < 2) return errorResponse(event);

        RulesAction playlistAction = RulesAction.of(event.getArguments().get(1));
        switch (playlistAction) {
            case ON: return channelRulesActivateAction.apply(event);
            case OFF: return channelRulesDeactivateAction.apply(event);
            case SHOW: return channelRulesShowAction.apply(event);
            default: return errorResponse(event);
        }
    }

    private Mono<? extends Response> errorResponse(ChannelRulesEvent event) {
        return event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel,
                        "Unknown rules action, try one of the following: **ON**, **OFF**, **SHOW**", false));
    }

    private enum RulesAction {
        ON, OFF, SHOW, UNKNOWN;

        public static RulesAction of(String string){
            for (RulesAction value : values()) {
                if(value.name().equalsIgnoreCase(string)) return value;
            }
            return UNKNOWN;
        }
    }
}
