package com.w1sh.medusa.listeners;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.data.responses.Embed;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.QueueTrackEvent;
import com.w1sh.medusa.utils.ResponseUtils;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Queue;

@Component
@RequiredArgsConstructor
public final class QueueTrackListener implements CustomEventListener<QueueTrackEvent> {

    private final AudioConnectionManager audioConnectionManager;
    private final ResponseDispatcher responseDispatcher;

    @Override
    public Mono<Void> execute(QueueTrackEvent event) {
        return audioConnectionManager.getAudioConnection(event)
                .map(con -> con.getTrackScheduler().getFullQueue())
                .flatMap(queue -> createPlaylistEmbed(event, queue))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    public Mono<Embed> createPlaylistEmbed(QueueTrackEvent event, Queue<AudioTrack> trackQueue){
        final long queueDuration = trackQueue.stream()
                .map(AudioTrack::getDuration)
                .reduce(Long::sum)
                .orElse(0L);
        final AudioTrack playingTrack = trackQueue.poll();

        return event.getChannel().map(channel -> new Embed(channel, embedCreateSpec -> {
            embedCreateSpec.setColor(Color.GREEN);
            embedCreateSpec.setTitle(":notes:\tQueued tracks");
            if (playingTrack != null) {
                embedCreateSpec.addField("Currently playing",
                        String.format("**%s**%n[%s](%s) | %s",
                                playingTrack.getInfo().author,
                                playingTrack.getInfo().title,
                                playingTrack.getInfo().uri,
                                ResponseUtils.formatDuration(playingTrack.getInfo().length)), true);
            }
            int queuePosition = 0;
            if(!trackQueue.isEmpty()) embedCreateSpec.addField(ResponseUtils.ZERO_WIDTH_SPACE,
                    ":arrow_down: **Queue** :arrow_down:", false);
            for (AudioTrack track : trackQueue) {
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
                    trackQueue.size(), ResponseUtils.formatDuration(queueDuration)), null);
        }));
    }
}
