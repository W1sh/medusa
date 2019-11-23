package com.w1sh.medusa.api.audio.listeners;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.w1sh.medusa.api.audio.events.QueueTrackEvent;
import com.w1sh.medusa.audio.AudioConnection;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.core.managers.AudioConnectionManager;
import com.w1sh.medusa.utils.Messenger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.util.Optional;
import java.util.Queue;

@Component
public class QueueTrackListener implements EventListener<QueueTrackEvent> {

    public QueueTrackListener(CommandEventDispatcher eventDispatcher) {
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<QueueTrackEvent> getEventType() {
        return QueueTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(QueueTrackEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .flatMap(AudioConnectionManager.getInstance()::getAudioConnection)
                .map(AudioConnection::getTrackScheduler)
                .zipWith(event.getMessage().getChannel())
                .flatMap(tuple -> Messenger.send(tuple.getT2(), embedCreateSpec -> {
                    embedCreateSpec.setColor(Color.GREEN);
                    embedCreateSpec.setTitle(":notes:\tQueued tracks");
                    final Optional<AudioTrack> playingTrack = tuple.getT1().getPlayingTrack();
                    final Queue<AudioTrack> queue = tuple.getT1().getQueue();
                    playingTrack.ifPresent(audioTrack -> embedCreateSpec.addField("Currently playing",
                            String.format("**%s**%n[%s](%s) | %s",
                                    audioTrack.getInfo().author,
                                    audioTrack.getInfo().title,
                                    audioTrack.getInfo().uri,
                                    Messenger.formatDuration(audioTrack.getInfo().length)), true));
                    int queuePosition = 0;
                    if(!queue.isEmpty()) embedCreateSpec.addField(Messenger.ZERO_WIDTH_SPACE,
                            ":arrow_down: **Queue** :arrow_down:", false);
                    for (AudioTrack track : queue) {
                        if(queuePosition < 5) {
                            queuePosition++;
                            embedCreateSpec.addField(String.format("**%s**", track.getInfo().author), String.format("**%d**\t[%s](%s) | %s",
                                    queuePosition,
                                    track.getInfo().title,
                                    track.getInfo().uri,
                                    Messenger.formatDuration(track.getInfo().length)), false);
                        } else break;
                    }
                    embedCreateSpec.setFooter(String.format("%d queued tracks", queue.size()), null);
                }))
                .then();
    }
}
