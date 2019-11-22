package com.w1sh.medusa.api.audio.listeners;

import com.w1sh.medusa.api.audio.events.NextTrackEvent;
import com.w1sh.medusa.audio.AudioConnection;
import com.w1sh.medusa.audio.TrackScheduler;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.core.managers.AudioConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class NextTrackListener implements EventListener<NextTrackEvent> {

    private static final Logger logger = LoggerFactory.getLogger(NextTrackListener.class);

    public NextTrackListener(CommandEventDispatcher eventDispatcher) {
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<NextTrackEvent> getEventType() {
        return NextTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(NextTrackEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .flatMap(AudioConnectionManager.getInstance()::getAudioConnection)
                .map(AudioConnection::getTrackScheduler)
                .doOnNext(TrackScheduler::nextTrack)
                .then();
    }
}
