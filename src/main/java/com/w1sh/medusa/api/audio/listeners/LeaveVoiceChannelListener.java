package com.w1sh.medusa.api.audio.listeners;

import com.w1sh.medusa.api.audio.events.LeaveVoiceChannelEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.core.managers.AudioConnectionManager;
import com.w1sh.medusa.utils.Messager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@DependsOn({"audioConnectionManager"})
@Component
public class LeaveVoiceChannelListener implements EventListener<LeaveVoiceChannelEvent> {

    @Value("${event.voice.leave}")
    private String voiceLeave;

    public LeaveVoiceChannelListener(CommandEventDispatcher eventDispatcher) {
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
                .flatMap(channel -> Messager.send(event, voiceLeave))
                .then();
    }
}
