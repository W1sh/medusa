package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.ResumeTrackEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class ResumeTrackListener implements EventListener<ResumeTrackEvent> {

    private final AudioConnectionManager audioConnectionManager;

    public ResumeTrackListener(AudioConnectionManager audioConnectionManager) {
        this.audioConnectionManager = audioConnectionManager;
    }

    @Override
    public Class<ResumeTrackEvent> getEventType() {
        return ResumeTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(ResumeTrackEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .flatMap(audioConnectionManager::getAudioConnection)
                .zipWith(event.getMessage().getChannel(), (con, mc) -> con.getTrackScheduler().resume(mc))
                .then();
    }
}
