package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.ResumeTrackEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class ResumeTrackListener implements EventListener<ResumeTrackEvent> {

    private final AudioConnectionManager audioConnectionManager;

    @Override
    public Class<ResumeTrackEvent> getEventType() {
        return ResumeTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(ResumeTrackEvent event) {
        return audioConnectionManager.getAudioConnection(event)
                .doOnNext(con -> con.getTrackScheduler().resume())
                .then();
    }
}
