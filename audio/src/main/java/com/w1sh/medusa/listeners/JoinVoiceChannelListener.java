package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.events.JoinVoiceChannelEvent;
import com.w1sh.medusa.services.MessageService;
import discord4j.core.object.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class JoinVoiceChannelListener implements CustomEventListener<JoinVoiceChannelEvent> {

    private final MessageService messageService;
    private final AudioConnectionManager audioConnectionManager;

    @Override
    public Mono<Void> execute(JoinVoiceChannelEvent event) {
        return Mono.justOrEmpty(event)
                .filterWhen(ev -> Mono.justOrEmpty(ev.getMember())
                        .flatMap(Member::getVoiceState)
                        .hasElement())
                .flatMap(audioConnectionManager::joinVoiceChannel)
                .flatMap(audioConnection -> messageService.send(event.getChannel(), MessageEnum.JOIN_SUCCESS))
                .switchIfEmpty(messageService.send(event.getChannel(), MessageEnum.JOIN_ERROR))
                .then();
    }
}
