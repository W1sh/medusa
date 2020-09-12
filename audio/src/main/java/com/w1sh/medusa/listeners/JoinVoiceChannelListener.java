package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.JoinVoiceChannelEvent;
import discord4j.core.object.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class JoinVoiceChannelListener implements CustomEventListener<JoinVoiceChannelEvent> {

    @Value("${event.voice.join}")
    private String voiceJoin;

    private final ResponseDispatcher responseDispatcher;
    private final AudioConnectionManager audioConnectionManager;

    @Override
    public Mono<Void> execute(JoinVoiceChannelEvent event) {
        return Mono.justOrEmpty(event)
                .filterWhen(ev -> Mono.justOrEmpty(ev.getMember())
                        .flatMap(Member::getVoiceState)
                        .hasElement())
                .flatMap(audioConnectionManager::joinVoiceChannel)
                .flatMap(audioConnection -> createJoinSuccessMessage(event))
                .switchIfEmpty(createEmptyVoiceStateErrorMessage(event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<TextMessage> createEmptyVoiceStateErrorMessage(JoinVoiceChannelEvent event){
        return event.getChannel().map(chan -> new TextMessage(chan, String.format("**%s**, you are not in a voice channel!",
                        event.getNickname()), false));
    }

    private Mono<TextMessage> createJoinSuccessMessage(JoinVoiceChannelEvent event){
        return event.getChannel().map(channel -> new TextMessage(channel, voiceJoin, false));
    }
}
