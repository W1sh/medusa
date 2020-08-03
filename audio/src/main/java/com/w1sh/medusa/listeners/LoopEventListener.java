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
public final class LoopEventListener implements EventListener<LoopEvent> {

    private final AudioConnectionManager audioConnectionManager;
    private final ResponseDispatcher responseDispatcher;

    @Override
    public Mono<Void> execute(LoopEvent event) {
        LoopAction loopAction = LoopAction.of(event.getArguments().get(0));

        return audioConnectionManager.getAudioConnection(event)
                .doOnNext(con -> con.getTrackScheduler().loop(loopAction))
                .flatMap(la -> changeLoopModeMessage(event, loopAction))
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
