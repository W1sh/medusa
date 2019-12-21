package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.events.CommandEventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.core.managers.PermissionManager;
import com.w1sh.medusa.events.StopTrackEvent;
import com.w1sh.medusa.utils.Messenger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.awt.*;

@Component
public class StopTrackListener implements EventListener<StopTrackEvent> {

    public StopTrackListener(CommandEventDispatcher eventDispatcher) {
        CommandEventFactory.registerEvent(StopTrackEvent.KEYWORD, StopTrackEvent.class);
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<StopTrackEvent> getEventType() {
        return StopTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(StopTrackEvent event) {
        return Mono.justOrEmpty(event)
                .filterWhen(ev -> PermissionManager.getInstance().hasPermissions(ev, ev.getPermissions()))
                .flatMap(ev -> Mono.justOrEmpty(ev.getGuildId()))
                .flatMap(AudioConnectionManager.getInstance()::getAudioConnection)
                .doOnNext(audioConnection -> {
                    final int queueSize = audioConnection.getTrackScheduler().getQueue().size();
                    audioConnection.getTrackScheduler().stopQueue();
                    event.getMessage().getChannel().flatMap(channel -> Messenger.send(channel, embedCreateSpec ->
                            embedCreateSpec.setTitle(":stop_button:\tStopped queue")
                                    .setColor(Color.GREEN)
                                    .setDescription(String.format(
                                            "Stopped playing **%s**%n%nCleared **%d** tracks from queue. Queue is now empty.",
                                            audioConnection.getTrackScheduler().getPlayingTrack().map(track -> track.getInfo().title).orElse(""),
                                            queueSize))
                                    .setFooter("The bot will automatically leave after 2 min unless new tracks are added.", null)))
                            .subscribe();
                })
                .flatMap(a -> Mono.justOrEmpty(event.getGuildId()))
                .flatMap(AudioConnectionManager.getInstance()::scheduleLeave)
                .then();
    }
}
