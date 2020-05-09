package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.RewindTrackEvent;
import discord4j.rest.util.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

@Component
public final class RewindTrackListener implements EventListener<RewindTrackEvent> {

    private static final Logger logger = LoggerFactory.getLogger(RewindTrackListener.class);

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
    private final AudioConnectionManager audioConnectionManager;

    public RewindTrackListener(AudioConnectionManager audioConnectionManager) {
        this.audioConnectionManager = audioConnectionManager;
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public Class<RewindTrackEvent> getEventType() {
        return RewindTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(RewindTrackEvent event) {
        Snowflake guildId = event.getGuildId().orElse(Snowflake.of(0));

        return Mono.justOrEmpty(event.getArguments().get(0))
                .handle(this::parseTime)
                .zipWith(audioConnectionManager.getAudioConnection(guildId), (time, ac) -> ac.getTrackScheduler().rewind(time))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> logger.error("Failed to rewind track to requested time <{}>",
                        event.getArguments().get(0), throwable)))
                .then();
    }

    private void parseTime(String time, SynchronousSink<Long> sink) {
        try {
            long milliseconds = simpleDateFormat.parse(time).getTime();
            sink.next(milliseconds);
        } catch (ParseException e) {
            sink.error(e);
        }
    }
}
