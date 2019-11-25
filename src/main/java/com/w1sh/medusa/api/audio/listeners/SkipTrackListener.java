package com.w1sh.medusa.api.audio.listeners;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.w1sh.medusa.api.audio.events.SkipTrackEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.core.managers.AudioConnectionManager;
import com.w1sh.medusa.core.managers.PermissionManager;
import com.w1sh.medusa.utils.Messenger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.util.Optional;

@Component
public class SkipTrackListener implements EventListener<SkipTrackEvent> {

    public SkipTrackListener(CommandEventDispatcher eventDispatcher) {
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<SkipTrackEvent> getEventType() {
        return SkipTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(SkipTrackEvent event) {
        return Mono.justOrEmpty(event)
                .filterWhen(ev -> PermissionManager.getInstance().hasPermissions(ev, ev.getPermissions()))
                .flatMap(ev -> Mono.justOrEmpty(ev.getGuildId()))
                .flatMap(AudioConnectionManager.getInstance()::getAudioConnection)
                .doOnNext(audioConnection -> {
                    final Optional<AudioTrack> audioTrack = audioConnection.getTrackScheduler().getPlayingTrack();
                    audioConnection.getTrackScheduler().nextTrack(true);
                    event.getMessage().getChannel().flatMap(channel -> Messenger.send(channel, embedCreateSpec ->
                            embedCreateSpec.setTitle(String.format(":track_next:\tSkipped track - %s",
                                    audioTrack.map(track -> track.getInfo().title).orElse("")))
                                    .setColor(Color.GREEN)))
                            .subscribe();
                })
                .then();
    }
}
