package com.w1sh.medusa.listeners;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.events.CommandEventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.core.managers.PermissionManager;
import com.w1sh.medusa.events.SkipTrackEvent;
import com.w1sh.medusa.utils.Messenger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.util.Optional;

@Component
public class SkipTrackListener implements EventListener<SkipTrackEvent> {

    public SkipTrackListener(CommandEventDispatcher eventDispatcher) {
        CommandEventFactory.registerEvent(SkipTrackEvent.KEYWORD, SkipTrackEvent.class);
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
                            embedCreateSpec.setTitle(":track_next:\tSkipped track")
                                    .setDescription(String.format("[%s](%s)",
                                            audioTrack.map(track -> track.getInfo().title).orElse(""),
                                            audioTrack.map(track -> track.getInfo().uri).orElse("")))
                                    .setColor(Color.GREEN)))
                            .subscribe();
                })
                .then();
    }
}
