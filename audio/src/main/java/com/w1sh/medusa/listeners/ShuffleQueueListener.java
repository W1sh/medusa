package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.ShuffleQueueEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class ShuffleQueueListener implements EventListener<ShuffleQueueEvent> {

    private final ResponseDispatcher responseDispatcher;
    private final AudioConnectionManager audioConnectionManager;

    public ShuffleQueueListener(ResponseDispatcher responseDispatcher, AudioConnectionManager audioConnectionManager) {
        this.responseDispatcher = responseDispatcher;
        this.audioConnectionManager = audioConnectionManager;
    }

    @Override
    public Class<ShuffleQueueEvent> getEventType() {
        return ShuffleQueueEvent.class;
    }

    @Override
    public Mono<Void> execute(ShuffleQueueEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .flatMap(audioConnectionManager::getAudioConnection)
                .doOnNext(audioConnection -> audioConnection.getTrackScheduler().shuffle())
                .flatMap(audioConnection -> createShuffleMessage(event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<TextMessage> createShuffleMessage(ShuffleQueueEvent event) {
        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, "The queue has been shuffled!", false));
    }
}
