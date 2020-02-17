package com.w1sh.medusa.listeners;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.w1sh.medusa.AudioConnection;
import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.TrackScheduler;
import com.w1sh.medusa.data.responses.Embed;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.QueueTrackEvent;
import com.w1sh.medusa.utils.ResponseUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.util.Optional;
import java.util.Queue;

@Component
public final class QueueTrackListener implements EventListener<QueueTrackEvent> {

    private final ResponseDispatcher responseDispatcher;
    private final AudioConnectionManager audioConnectionManager;

    public QueueTrackListener(ResponseDispatcher responseDispatcher, AudioConnectionManager audioConnectionManager) {
        this.responseDispatcher = responseDispatcher;
        this.audioConnectionManager = audioConnectionManager;
    }

    @Override
    public Class<QueueTrackEvent> getEventType() {
        return QueueTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(QueueTrackEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .flatMap(audioConnectionManager::getAudioConnection)
                .map(AudioConnection::getTrackScheduler)
                .flatMap(trackScheduler -> createQueueEmbed(trackScheduler, event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    public Mono<Embed> createQueueEmbed(TrackScheduler trackScheduler, QueueTrackEvent event){
        return event.getMessage().getChannel()
                .map(chan -> new Embed(chan, embedCreateSpec -> {
                    embedCreateSpec.setColor(Color.GREEN);
                    embedCreateSpec.setTitle(":notes:\tQueued tracks");
                    final Optional<AudioTrack> playingTrack = trackScheduler.getPlayingTrack();
                    final Queue<AudioTrack> queue = trackScheduler.getQueue();
                    playingTrack.ifPresent(audioTrack -> embedCreateSpec.addField("Currently playing",
                            String.format("**%s**%n[%s](%s) | %s",
                                    audioTrack.getInfo().author,
                                    audioTrack.getInfo().title,
                                    audioTrack.getInfo().uri,
                                    ResponseUtils.formatDuration(audioTrack.getInfo().length)), true));
                    int queuePosition = 0;
                    if(!queue.isEmpty()) embedCreateSpec.addField(ResponseUtils.ZERO_WIDTH_SPACE,
                            ":arrow_down: **Queue** :arrow_down:", false);
                    for (AudioTrack track : queue) {
                        if(queuePosition < 5) {
                            queuePosition++;
                            embedCreateSpec.addField(String.format("**%s**", track.getInfo().author), String.format("**%d**\t[%s](%s) | %s",
                                    queuePosition,
                                    track.getInfo().title,
                                    track.getInfo().uri,
                                    ResponseUtils.formatDuration(track.getInfo().length)), false);
                        } else break;
                    }
                    embedCreateSpec.setFooter(String.format("%d queued tracks | Queue duration: %s",
                            queue.size(),
                            ResponseUtils.formatDuration(trackScheduler.getQueueDuration())), null);
                }));
    }
}
