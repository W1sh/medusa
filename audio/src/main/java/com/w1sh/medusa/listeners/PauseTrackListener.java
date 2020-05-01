package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.PauseTrackEvent;
import discord4j.core.object.entity.channel.GuildChannel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class PauseTrackListener implements EventListener<PauseTrackEvent> {

    private final AudioConnectionManager audioConnectionManager;

    public PauseTrackListener(AudioConnectionManager audioConnectionManager) {
        this.audioConnectionManager = audioConnectionManager;
    }

    @Override
    public Class<PauseTrackEvent> getEventType() {
        return PauseTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(PauseTrackEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .flatMap(audioConnectionManager::getAudioConnection)
                .flatMap(audioConnection -> event.getMessage().getChannel()
                        .doOnNext(messageChannel -> {
                            audioConnection.getTrackScheduler().updateResponseChannel((GuildChannel) messageChannel);
                            audioConnection.getTrackScheduler().pause();
                        }))
                .then();
    }
}
