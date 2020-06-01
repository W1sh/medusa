package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.RewindTrackEvent;
import discord4j.common.util.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static com.w1sh.medusa.utils.Reactive.isEmpty;

@Component
public final class RewindTrackListener implements EventListener<RewindTrackEvent> {

    private static final Logger logger = LoggerFactory.getLogger(RewindTrackListener.class);

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
    private final AudioConnectionManager audioConnectionManager;
    private final ResponseDispatcher responseDispatcher;

    public RewindTrackListener(AudioConnectionManager audioConnectionManager, ResponseDispatcher responseDispatcher) {
        this.audioConnectionManager = audioConnectionManager;
        this.responseDispatcher = responseDispatcher;
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
                .onErrorResume(t -> Mono.fromRunnable(() -> logger.error("Failed to rewind track to requested time <{}>", event.getArguments().get(0), t)))
                .transform(isEmpty())
                .flatMap(b -> createErrorMessage(event))
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

    private Mono<TextMessage> createErrorMessage(RewindTrackEvent event){
        return event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel, ":x: Invalid argument received, the argument must be of type **minutes : seconds**", false))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush);
    }
}
