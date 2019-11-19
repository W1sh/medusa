package com.w1sh.medusa.api.audio.listeners;

import com.w1sh.medusa.api.audio.events.JoinVoiceChannelEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.core.managers.AudioConnectionManager;
import com.w1sh.medusa.utils.Messager;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@DependsOn({"audioConnectionManager"})
@Component
public class JoinVoiceChannelListener implements EventListener<JoinVoiceChannelEvent> {

    public JoinVoiceChannelListener(CommandEventDispatcher eventDispatcher) {
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<JoinVoiceChannelEvent> getEventType() {
        return JoinVoiceChannelEvent.class;
    }

    @Override
    public Mono<Void> execute(JoinVoiceChannelEvent event) {
        return Mono.justOrEmpty(event.getMember())
                .flatMap(Member::getVoiceState)
                .flatMap(VoiceState::getChannel)
                .flatMap(AudioConnectionManager.getInstance()::joinVoiceChannel)
                .flatMap(conn -> event.getMessage().getChannel())
                .flatMap(channel -> Messager.send(channel.getClient(), channel, "Joining voice channel!"))
                .then();
    }
}
