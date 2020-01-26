package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.core.data.TextMessage;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.events.LeaveVoiceChannelEvent;
import discord4j.core.object.entity.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@DependsOn({"audioConnectionManager"})
@Component
public final class LeaveVoiceChannelListener implements EventListener<LeaveVoiceChannelEvent> {

    @Value("${event.voice.leave}")
    private String voiceLeave;

    private final ResponseDispatcher responseDispatcher;

    public LeaveVoiceChannelListener(CommandEventDispatcher eventDispatcher, ResponseDispatcher responseDispatcher) {
        this.responseDispatcher = responseDispatcher;
        EventFactory.registerEvent(LeaveVoiceChannelEvent.KEYWORD, LeaveVoiceChannelEvent.class);
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<LeaveVoiceChannelEvent> getEventType() {
        return LeaveVoiceChannelEvent.class;
    }

    @Override
    public Mono<Void> execute(LeaveVoiceChannelEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .flatMap(AudioConnectionManager.getInstance()::leaveVoiceChannel)
                .flatMap(bool -> {
                    if (Boolean.TRUE.equals(bool)) {
                        return createLeaveSuccessMessage(event);
                    } else return createNoVoiceStateErrorMessage(event);
                })
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<TextMessage> createNoVoiceStateErrorMessage(LeaveVoiceChannelEvent event){
        return event.getMessage().getChannel()
                .zipWith(Mono.justOrEmpty(event.getMember().flatMap(Member::getNickname)))
                .map(tuple -> new TextMessage(tuple.getT1(), String.format("**%s**, I'm not in a voice channel",
                        tuple.getT2()), false));
    }

    private Mono<TextMessage> createLeaveSuccessMessage(LeaveVoiceChannelEvent event){
        return event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel, voiceLeave, false));
    }
}
