package com.w1sh.medusa.listeners;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.w1sh.medusa.AudioConnection;
import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.data.events.EventFactory;
import com.w1sh.medusa.data.responses.Embed;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.SkipTrackEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.util.Optional;

@Component
public final class SkipTrackListener implements EventListener<SkipTrackEvent> {

    private final ResponseDispatcher responseDispatcher;

    public SkipTrackListener(ResponseDispatcher responseDispatcher) {
        this.responseDispatcher = responseDispatcher;
    }

    @Override
    public Class<SkipTrackEvent> getEventType() {
        return SkipTrackEvent.class;
    }

    @Override
    public Mono<Void> execute(SkipTrackEvent event) {
        return Mono.justOrEmpty(event)
                .flatMap(ev -> Mono.justOrEmpty(ev.getGuildId()))
                .flatMap(AudioConnectionManager.getInstance()::getAudioConnection)
                .flatMap(audioConnection -> createSkipMessage(audioConnection, event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    public Mono<Embed> createSkipMessage(AudioConnection audioConnection, SkipTrackEvent event){
        final Optional<AudioTrack> audioTrack = audioConnection.getTrackScheduler().getPlayingTrack();
        audioConnection.getTrackScheduler().nextTrack(true);

        return event.getMessage().getChannel()
                .map(channel -> new Embed(channel, embedCreateSpec ->
                        embedCreateSpec.setTitle(":track_next:\tSkipped track")
                                .setDescription(String.format("[%s](%s)",
                                        audioTrack.map(track -> track.getInfo().title).orElse(""),
                                        audioTrack.map(track -> track.getInfo().uri).orElse("")))
                                .setColor(Color.GREEN)));
    }
}
