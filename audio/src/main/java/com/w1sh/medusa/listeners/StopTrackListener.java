package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnection;
import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.data.events.EventFactory;
import com.w1sh.medusa.data.responses.Embed;
import com.w1sh.medusa.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.StopTrackEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.awt.*;

@Component
public final class StopTrackListener implements EventListener<StopTrackEvent> {

    private final ResponseDispatcher responseDispatcher;

    public StopTrackListener(CommandEventDispatcher eventDispatcher, ResponseDispatcher responseDispatcher) {
        this.responseDispatcher = responseDispatcher;
        EventFactory.registerEvent(StopTrackEvent.KEYWORD, StopTrackEvent.class);
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<StopTrackEvent> getEventType() {
        return StopTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(StopTrackEvent event) {
        return Mono.justOrEmpty(event)
                .flatMap(ev -> Mono.justOrEmpty(ev.getGuildId()))
                .flatMap(AudioConnectionManager.getInstance()::getAudioConnection)
                .flatMap(audioConnection -> createStopMessage(audioConnection, event))
                .doOnNext(responseDispatcher::queue)
                .flatMap(a -> Mono.justOrEmpty(event.getGuildId()))
                .flatMap(AudioConnectionManager.getInstance()::scheduleLeave)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    public Mono<Embed> createStopMessage(AudioConnection audioConnection, StopTrackEvent event){
        final int queueSize = audioConnection.getTrackScheduler().getQueue().size();
        audioConnection.getTrackScheduler().stopQueue();
        return event.getMessage().getChannel()
                .filter(c -> audioConnection.getTrackScheduler().getPlayingTrack().isPresent())
                .map(channel -> new Embed(channel, embedCreateSpec ->
                        embedCreateSpec.setTitle(":stop_button:\tStopped queue")
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
                .map(channel -> new Embed(channel, embedCreateSpec ->
                        embedCreateSpec.setTitle(":stop_button:\tStopped queue")
                                .setColor(Color.GREEN)
                                .setDescription(String.format("Stopped queue%n%nCleared all tracks from queue. Queue is now empty."))
                                .setFooter("The bot will automatically leave after 2 min unless new tracks are added.", null)));
    }
}
