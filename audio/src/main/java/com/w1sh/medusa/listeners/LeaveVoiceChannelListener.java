package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.LeaveVoiceChannelEvent;
import com.w1sh.medusa.utils.Reactive;
import discord4j.core.object.entity.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class LeaveVoiceChannelListener implements EventListener<LeaveVoiceChannelEvent> {

    @Value("${event.voice.leave}")
    private String voiceLeave;

    private final ResponseDispatcher responseDispatcher;
    private final AudioConnectionManager audioConnectionManager;

    public LeaveVoiceChannelListener(ResponseDispatcher responseDispatcher, AudioConnectionManager audioConnectionManager) {
        this.responseDispatcher = responseDispatcher;
        this.audioConnectionManager = audioConnectionManager;
    }

    @Override
    public Class<LeaveVoiceChannelEvent> getEventType() {
        return LeaveVoiceChannelEvent.class;
    }

    @Override
    public Mono<Void> execute(LeaveVoiceChannelEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .flatMap(audioConnectionManager::leaveVoiceChannel)
                .transform(Reactive.ifElse(b -> createLeaveSuccessMessage(event), b-> createNoVoiceStateErrorMessage(event)))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<TextMessage> createNoVoiceStateErrorMessage(LeaveVoiceChannelEvent event){
        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, String.format("**%s**, I'm not in a voice channel",
                        event.getMember().flatMap(Member::getNickname).orElse("You")), false));
    }

    private Mono<TextMessage> createLeaveSuccessMessage(LeaveVoiceChannelEvent event){
        return event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel, voiceLeave, false));
    }
}
