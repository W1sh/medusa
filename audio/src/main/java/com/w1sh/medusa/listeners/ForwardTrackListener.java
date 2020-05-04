package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.events.ForwardTrackEvent;
import discord4j.core.object.entity.channel.GuildChannel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

@Component
public final class ForwardTrackListener implements EventListener<ForwardTrackEvent> {

    private final AudioConnectionManager audioConnectionManager;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

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
        return Mono.justOrEmpty(event.getArguments().get(0))
                .handle(this::parseTime)
                .zipWith(audioConnectionManager.getAudioConnection(event.getGuildId().orElseThrow()))
                .flatMap(tuple -> event.getMessage().getChannel()
                        .doOnNext(messageChannel -> {
                            tuple.getT2().getTrackScheduler().updateResponseChannel((GuildChannel) messageChannel);
                            tuple.getT2().getTrackScheduler().forward(tuple.getT1());
                        }))
                .then();
    }

    private void parseTime(String time, SynchronousSink<Long> sink) {
        try {
            long miliseconds = simpleDateFormat.parse(time).getTime();
            sink.next(miliseconds);
        } catch (ParseException e) {
            sink.error(e);
        }
    }
}
