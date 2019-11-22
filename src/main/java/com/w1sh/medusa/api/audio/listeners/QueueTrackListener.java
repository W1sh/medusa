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
import java.util.concurrent.TimeUnit;

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
                    embedCreateSpec.setTitle(":notes:\tQueued tracks\n");
                    final Optional<AudioTrack> playingTrack = tuple.getT1().getPlayingTrack();
                    final Queue<AudioTrack> queue = tuple.getT1().getQueue();
                    playingTrack.ifPresent(audioTrack -> embedCreateSpec.addField("Currently playing",
                            String.format("**%s**%n%n[%s](%s) | %d:%d",
                                    audioTrack.getInfo().author,
                                    audioTrack.getInfo().title,
                                    audioTrack.getInfo().uri,
                                    TimeUnit.MILLISECONDS.toMinutes(audioTrack.getInfo().length),
                                    TimeUnit.MILLISECONDS.toSeconds(audioTrack.getInfo().length) -
                                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(audioTrack.getInfo().length))), true));
                    int queuePosition = 0;
                    for (AudioTrack track : queue) {
                        queuePosition++;
                        embedCreateSpec.addField(String.format("**%d**", queuePosition), String.format("%n**%s**%n%n[%s](%s) | %d:%d",
                                track.getInfo().author,
                                track.getInfo().title,
                                track.getInfo().uri,
                                TimeUnit.MILLISECONDS.toMinutes(track.getInfo().length),
                                TimeUnit.MILLISECONDS.toSeconds(track.getInfo().length) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(track.getInfo().length))), true);
                    }
                    embedCreateSpec.setFooter(String.format("%d queued tracks", queue.size()), null);
                }))
                .then();
    }
}
