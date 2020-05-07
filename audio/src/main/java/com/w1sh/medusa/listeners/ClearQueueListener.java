package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.ClearQueueEvent;
import discord4j.core.object.entity.channel.GuildChannel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class ClearQueueListener implements EventListener<ClearQueueEvent> {

    private final AudioConnectionManager audioConnectionManager;

    public ClearQueueListener(AudioConnectionManager audioConnectionManager) {
        this.audioConnectionManager = audioConnectionManager;
    }

    @Override
    public Class<ClearQueueEvent> getEventType() {
        return ClearQueueEvent.class;
    }

    @Override
    public Mono<Void> execute(ClearQueueEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .flatMap(audioConnectionManager::getAudioConnection)
                .flatMap(audioConnection -> event.getMessage().getChannel()
                        .doOnNext(messageChannel -> {
                            audioConnection.getTrackScheduler().updateResponseChannel((GuildChannel) messageChannel);
                            audioConnection.getTrackScheduler().clearQueue();
                        }))
                .then();
    }
}
