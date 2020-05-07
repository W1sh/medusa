package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.ForwardTrackEvent;
import discord4j.rest.util.Snowflake;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

@Component
public final class ForwardTrackListener implements EventListener<ForwardTrackEvent> {

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
    private final AudioConnectionManager audioConnectionManager;

    public ForwardTrackListener(AudioConnectionManager audioConnectionManager) {
        this.audioConnectionManager = audioConnectionManager;
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public Class<ForwardTrackEvent> getEventType() {
        return ForwardTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(ForwardTrackEvent event) {
        Snowflake guildId = event.getGuildId().orElse(Snowflake.of(0));

        return Mono.justOrEmpty(event.getArguments().get(0))
                .handle(this::parseTime)
                .zipWith(audioConnectionManager.getAudioConnection(guildId))
                .doOnNext(tuple -> tuple.getT2().getTrackScheduler().forward(tuple.getT1()))
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
