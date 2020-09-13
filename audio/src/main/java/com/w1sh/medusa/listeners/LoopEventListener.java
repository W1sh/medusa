package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.data.LoopAction;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.events.LoopEvent;
import com.w1sh.medusa.services.MessageService;
import discord4j.core.object.entity.Message;
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
                .then();
    }

    private Mono<Message> changeLoopModeMessage(LoopEvent event, LoopAction loopAction){
        if (!LoopAction.UNKNOWN.equals(loopAction)) {
            return messageService.send(event.getChannel(), MessageEnum.LOOP_SUCCESS);
        } else return messageService.send(event.getChannel(), MessageEnum.LOOP_ERROR);
    }
}
