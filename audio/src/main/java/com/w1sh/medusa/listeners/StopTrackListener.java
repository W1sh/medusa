package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnection;
import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.data.responses.Embed;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.StopTrackEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.awt.*;

@Component
public final class StopTrackListener implements EventListener<StopTrackEvent> {

    private final ResponseDispatcher responseDispatcher;
    private final AudioConnectionManager audioConnectionManager;

    public StopTrackListener(ResponseDispatcher responseDispatcher, AudioConnectionManager audioConnectionManager) {
        this.responseDispatcher = responseDispatcher;
        this.audioConnectionManager = audioConnectionManager;
    }

    @Override
    public Class<StopTrackEvent> getEventType() {
        return StopTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(StopTrackEvent event) {
        Mono<Embed> embedMono = Mono.justOrEmpty(event.getGuildId())
                .flatMap(audioConnectionManager::getAudioConnection)
                .flatMap(audioConnection -> createStopMessage(audioConnection, event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush);

        Mono<Void> scheduleLeaveMono = Mono.justOrEmpty(event.getGuildId())
                .flatMap(audioConnectionManager::scheduleLeave)
                .then();

        return embedMono.then(scheduleLeaveMono);
    }

    public Mono<Embed> createStopMessage(AudioConnection audioConnection, StopTrackEvent event){
        final int queueSize = audioConnection.getTrackScheduler().getQueue().size();
        audioConnection.getTrackScheduler().stopQueue();
        return event.getMessage().getChannel()
                .filter(c -> audioConnection.getTrackScheduler().getPlayingTrack().isPresent())
                .map(channel -> new Embed(channel, embedCreateSpec -> embedCreateSpec.setTitle(":stop_button:\tStopped queue")
                        .setColor(Color.GREEN)
                        .setDescription(String.format(
                                "Stopped playing **%s**%n%nCleared **%d** tracks from queue. Queue is now empty.",
                                audioConnection.getTrackScheduler().getPlayingTrack().map(track -> track.getInfo().title).orElse(""),
                                queueSize))
                        .setFooter("The bot will automatically leave after 2 min unless new tracks are added.", null)))
                .switchIfEmpty(createEmptyQueueStopMessage(event));
    }

    public Mono<Embed> createEmptyQueueStopMessage(StopTrackEvent event){
        return event.getMessage().getChannel()
                .map(channel -> new Embed(channel, embedCreateSpec -> embedCreateSpec.setTitle(":stop_button:\tStopped queue")
                        .setColor(Color.GREEN)
                        .setDescription(String.format("Stopped queue%n%nCleared all tracks from queue. Queue is now empty."))
                        .setFooter("The bot will automatically leave after 2 min unless new tracks are added.", null)));
    }
}
