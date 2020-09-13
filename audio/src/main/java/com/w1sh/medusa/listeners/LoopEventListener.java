package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.data.LoopAction;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.events.LoopEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class LoopEventListener implements CustomEventListener<LoopEvent> {

    private final AudioConnectionManager audioConnectionManager;
    private final MessageService messageService;

    @Override
    public Mono<Void> execute(LoopEvent event) {
        final LoopAction loopAction = LoopAction.of(event.getArguments().get(0));

        return audioConnectionManager.getAudioConnection(event)
                .doOnNext(con -> con.getTrackScheduler().loop(loopAction))
                .flatMap(la -> changeLoopModeMessage(event, loopAction))
                .doOnNext(messageService::queue)
                .doAfterTerminate(messageService::flush)
                .then();
    }

    private Mono<TextMessage> changeLoopModeMessage(LoopEvent event, LoopAction loopAction){
        final String template = LoopAction.UNKNOWN.equals(loopAction) ?
                "Unknown loop mode, try one of the following: **TRACK**, **QUEUE**, **OFF**" :
                String.format("Changed loop mode to **%s**!", loopAction);

        return event.getChannel().map(chan -> new TextMessage(chan, template, false));
    }
}
