package com.w1sh.medusa.listeners;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.TrackScheduler;
import com.w1sh.medusa.core.data.Embed;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.events.PlayTrackEvent;
import com.w1sh.medusa.utils.Messenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.awt.*;

@Component
public final class PlayTrackListener implements EventListener<PlayTrackEvent> {

    private static final Logger logger = LoggerFactory.getLogger(PlayTrackListener.class);

    private final ResponseDispatcher responseDispatcher;

    public PlayTrackListener(CommandEventDispatcher eventDispatcher, ResponseDispatcher responseDispatcher) {
        this.responseDispatcher = responseDispatcher;
        EventFactory.registerEvent(PlayTrackEvent.KEYWORD, PlayTrackEvent.class);
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<PlayTrackEvent> getEventType() {
        return PlayTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(PlayTrackEvent event) {
        return Mono.justOrEmpty(event)
                .flatMap(tuple -> AudioConnectionManager.getInstance().requestTrack(event))
                .flatMap(trackScheduler -> createQueueMessage(trackScheduler, event))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> logger.error("Failed to delete playlist", throwable)))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<Embed> createQueueMessage(TrackScheduler trackScheduler, PlayTrackEvent event) {
        AudioTrack track = trackScheduler.getPlayingTrack().orElseThrow();
        return event.getMessage().getChannel()
                .map(chan -> new Embed(chan, embedCreateSpec -> embedCreateSpec.setTitle(":ballot_box_with_check:\tAdded to queue")
                        .setColor(Color.GREEN)
                        .addField(Messenger.ZERO_WIDTH_SPACE, String.format("**%s**%n[%s](%s) | %s",
                                track.getInfo().author,
                                track.getInfo().title,
                                track.getInfo().uri,
                                Messenger.formatDuration(track.getInfo().length)), true)));
    }
}
