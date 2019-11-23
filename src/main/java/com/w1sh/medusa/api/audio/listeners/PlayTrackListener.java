package com.w1sh.medusa.api.audio.listeners;

import com.w1sh.medusa.api.audio.events.PlayTrackEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.listeners.MultipleArgsEventListener;
import com.w1sh.medusa.core.managers.AudioConnectionManager;
import com.w1sh.medusa.core.managers.PermissionManager;
import com.w1sh.medusa.utils.Messenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.awt.*;

@Component
public class PlayTrackListener implements MultipleArgsEventListener<PlayTrackEvent> {

    private static final Logger logger = LoggerFactory.getLogger(PlayTrackListener.class);

    @Value("${event.voice.play}")
    private String voicePlay;
    @Value("${event.unsupported}")
    private String unsupported;

    public PlayTrackListener(CommandEventDispatcher eventDispatcher) {
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<PlayTrackEvent> getEventType() {
        return PlayTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(PlayTrackEvent event) {
        return Mono.justOrEmpty(event)
                .filterWhen(this::validate)
                .filterWhen(ev -> PermissionManager.getInstance().hasPermissions(ev, ev.getPermissions()))
                .flatMap(tuple -> AudioConnectionManager.getInstance().requestTrack(event))
                .doOnNext(scheduler -> scheduler.getPlayingTrack().ifPresent(track -> {
                    event.getMessage().getChannel()
                            .flatMap(channel -> Messenger.send(channel, embedCreateSpec ->
                                    embedCreateSpec.setTitle(":ballot_box_with_check:\tQueued track")
                                            .setColor(Color.GREEN)
                                            .addField(Messenger.ZERO_WIDTH_SPACE, String.format("**%s**%n[%s](%s) | %s",
                                                    track.getInfo().author,
                                                    track.getInfo().title,
                                                    track.getInfo().uri,
                                                    Messenger.formatDuration(track.getDuration())), true)))
                            .subscribe();
                }))
                .doOnError(throwable -> logger.error("Failed to play track", throwable))
                .then();
    }

    @Override
    public Mono<Boolean> validate(PlayTrackEvent event) {
        return Mono.justOrEmpty(event.getMessage().getContent())
                .map(content -> content.split(" "))
                .filter(split -> {
                    if(split.length != 2){
                        Messenger.send(event, unsupported).subscribe();
                        return false;
                    }
                    return true;
                })
                .hasElement();
    }
}
