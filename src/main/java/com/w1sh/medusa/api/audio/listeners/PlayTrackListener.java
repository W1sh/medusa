package com.w1sh.medusa.api.audio.listeners;

import com.w1sh.medusa.api.audio.events.PlayTrackEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.listeners.MultipleArgsEventListener;
import com.w1sh.medusa.core.managers.AudioConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PlayTrackListener implements MultipleArgsEventListener<PlayTrackEvent> {

    private static final Logger logger = LoggerFactory.getLogger(PlayTrackEvent.class);

    @Value("${event.voice.play}")
    private String voicePlay;

    public PlayTrackListener(CommandEventDispatcher eventDispatcher) {
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<PlayTrackEvent> getEventType() {
        return PlayTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(PlayTrackEvent event) {
        return Mono.justOrEmpty(event.getMessage().getContent())
                .zipWith(Mono.justOrEmpty(event.getGuildId()))
                .flatMap(tuple -> AudioConnectionManager.getInstance().requestTrack(tuple.getT1(), tuple.getT2()))
                //.log()
                //.doOnNext(audioTrack -> Messager.send(event, String.format(voicePlay, audioTrack.getIdentifier())))
                .doOnError(throwable -> logger.error("Failed to play track", throwable))
                /*.doFinally(signalType -> {
                    if(signalType.equals(SignalType.CANCEL) || signalType.equals(SignalType.ON_ERROR)){
                        logger.info("Error");
                    }
                })*/
                .then();
    }

    @Override
    public Mono<Boolean> validate(PlayTrackEvent event) {
        return null;
    }
}
