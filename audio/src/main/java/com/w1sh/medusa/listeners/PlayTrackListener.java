package com.w1sh.medusa.listeners;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.TrackScheduler;
import com.w1sh.medusa.data.events.EventFactory;
import com.w1sh.medusa.data.responses.Embed;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.PlayTrackEvent;
import com.w1sh.medusa.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.awt.*;

@Component
public final class PlayTrackListener implements EventListener<PlayTrackEvent> {

    private static final Logger logger = LoggerFactory.getLogger(PlayTrackListener.class);

    private final ResponseDispatcher responseDispatcher;

    public PlayTrackListener(ResponseDispatcher responseDispatcher) {
        this.responseDispatcher = responseDispatcher;
        EventFactory.registerEvent(PlayTrackEvent.KEYWORD, PlayTrackEvent.class);
    }

    @Override
    public Class<PlayTrackEvent> getEventType() {
        return PlayTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(PlayTrackEvent event) {
        return Mono.justOrEmpty(event)
                .flatMap(tuple -> AudioConnectionManager.getInstance().requestTrack(event))
                .filter(trackScheduler -> trackScheduler.getPlayingTrack().isPresent())
                .flatMap(trackScheduler -> createQueueMessage(trackScheduler, event))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> logger.error("Failed to play track", throwable)))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<Embed> createQueueMessage(TrackScheduler trackScheduler, PlayTrackEvent event) {
        AudioTrack track = trackScheduler.getPlayingTrack().orElseThrow();
        return event.getMessage().getChannel()
                .map(chan -> new Embed(chan, embedCreateSpec -> embedCreateSpec.setTitle(":ballot_box_with_check:\tAdded to queue")
                        .setColor(Color.GREEN)
                        .addField(ResponseUtils.ZERO_WIDTH_SPACE, String.format("**%s**%n[%s](%s) | %s",
                                track.getInfo().author,
                                track.getInfo().title,
                                track.getInfo().uri,
                                ResponseUtils.formatDuration(track.getInfo().length)), true)));
    }
}
