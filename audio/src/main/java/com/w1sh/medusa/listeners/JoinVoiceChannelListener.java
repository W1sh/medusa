package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.JoinVoiceChannelEvent;
import discord4j.core.object.entity.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class JoinVoiceChannelListener implements EventListener<JoinVoiceChannelEvent> {

    @Value("${event.voice.join}")
    private String voiceJoin;

    private final ResponseDispatcher responseDispatcher;

    public JoinVoiceChannelListener(ResponseDispatcher responseDispatcher) {
        this.responseDispatcher = responseDispatcher;
    }

    @Override
    public Class<JoinVoiceChannelEvent> getEventType() {
        return JoinVoiceChannelEvent.class;
    }

    @Override
    public Mono<Void> execute(JoinVoiceChannelEvent event) {
        return Mono.justOrEmpty(event)
                .filterWhen(ev -> Mono.justOrEmpty(ev.getMember())
                        .flatMap(Member::getVoiceState)
                        .hasElement())
                .flatMap(AudioConnectionManager.getInstance()::joinVoiceChannel)
                .flatMap(audioConnection -> createJoinSuccessMessage(event))
                .switchIfEmpty(createEmptyVoiceStateErrorMessage(event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<TextMessage> createEmptyVoiceStateErrorMessage(JoinVoiceChannelEvent event){
        return event.getMessage().getChannel()
                .zipWith(Mono.justOrEmpty(event.getMember().flatMap(Member::getNickname)))
                .map(tuple -> new TextMessage(tuple.getT1(), String.format("**%s**, you are not in a voice channel!",
                        tuple.getT2()), false));
    }

    private Mono<TextMessage> createJoinSuccessMessage(JoinVoiceChannelEvent event){
        return event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel, voiceJoin, false));
    }
}
