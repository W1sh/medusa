package com.w1sh.medusa.api.audio.listeners;

import com.w1sh.medusa.api.audio.events.StopTrackEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.core.managers.AudioConnectionManager;
import com.w1sh.medusa.core.managers.PermissionManager;
import com.w1sh.medusa.utils.Messenger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.awt.*;

@Component
public class StopTrackListener implements EventListener<StopTrackEvent> {

    public StopTrackListener(CommandEventDispatcher eventDispatcher) {
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
                                            "Cleared **%d** tracks from queue. Queue is now empty.%n%n" +
                                                    "The bot will automatically leave after **2** min unless new tracks are added.",
                                            queueSize))))
                            .subscribe();
                })
                .flatMap(a -> Mono.justOrEmpty(event.getGuildId()))
                .flatMap(AudioConnectionManager.getInstance()::scheduleLeave)
                .then();
    }
}
