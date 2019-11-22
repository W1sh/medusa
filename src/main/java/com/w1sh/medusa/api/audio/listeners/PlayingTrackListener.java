package com.w1sh.medusa.api.audio.listeners;

import com.w1sh.medusa.api.audio.events.PlayingTrackEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.core.managers.AudioConnectionManager;
import com.w1sh.medusa.utils.Messenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PlayingTrackListener implements EventListener<PlayingTrackEvent> {

    private static final Logger logger = LoggerFactory.getLogger(PlayingTrackListener.class);

    public PlayingTrackListener(CommandEventDispatcher eventDispatcher) {
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<PlayingTrackEvent> getEventType() {
        return PlayingTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(PlayingTrackEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .flatMap(AudioConnectionManager.getInstance()::getAudioConnection)
                .flatMap(audioConnection -> Messenger.send(event, String.format(":musical_note: Currently playing %s",
                        audioConnection.getTrackScheduler().getPlayer().getPlayingTrack().getInfo().title)))
                .then();
    }
}
