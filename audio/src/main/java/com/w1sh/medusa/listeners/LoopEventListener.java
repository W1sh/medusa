package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.data.LoopAction;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.LoopEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class LoopEventListener implements EventListener<LoopEvent> {

    private final AudioConnectionManager audioConnectionManager;
    private final ResponseDispatcher responseDispatcher;

    @Override
    public Class<LoopEvent> getEventType() {
        return LoopEvent.class;
    }

    @Override
    public Mono<Void> execute(LoopEvent event) {
        String loopMode = event.getArguments().get(0);

        return Mono.justOrEmpty(event.getGuildId())
                .flatMap(audioConnectionManager::getAudioConnection)
                .zipWith(event.getMessage().getChannel(), (con, mc) -> con.getTrackScheduler().loop(mc, loopMode))
                .flatMap(lm -> changeLoopModeMessage(event, lm))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<TextMessage> changeLoopModeMessage(LoopEvent event, LoopAction loopAction){
        final String template = LoopAction.UNKNOWN.equals(loopAction) ?
                "Unknown loop mode, try one of the following: **TRACK**, **QUEUE**, **OFF**" :
                String.format("Changed loop mode to **%s**!", loopAction);

        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, template, false));
    }
}
