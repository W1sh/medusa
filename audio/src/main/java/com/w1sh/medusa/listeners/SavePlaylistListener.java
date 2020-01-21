package com.w1sh.medusa.listeners;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.w1sh.medusa.AudioConnection;
import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.core.data.TextMessage;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.events.SavePlaylistEvent;
import com.w1sh.medusa.mongo.entities.Playlist;
import com.w1sh.medusa.mongo.services.PlaylistService;
import discord4j.core.object.entity.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public final class SavePlaylistListener implements EventListener<SavePlaylistEvent> {

    private static final Logger logger = LoggerFactory.getLogger(SavePlaylistListener.class);

    private final PlaylistService playlistService;
    private final ResponseDispatcher responseDispatcher;

    public SavePlaylistListener(ResponseDispatcher responseDispatcher, CommandEventDispatcher eventDispatcher, PlaylistService playlistService) {
        this.responseDispatcher = responseDispatcher;
        this.playlistService = playlistService;
        EventFactory.registerEvent(SavePlaylistEvent.KEYWORD, SavePlaylistEvent.class);
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<SavePlaylistEvent> getEventType() {
        return SavePlaylistEvent.class;
    }

    @Override
    public Mono<Void> execute(SavePlaylistEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .flatMap(AudioConnectionManager.getInstance()::getAudioConnection)
                .map(AudioConnection::getTrackScheduler)
                .flatMapIterable(trackScheduler -> {
                    List<AudioTrack> trackList = new ArrayList<>(trackScheduler.getQueue());
                    trackScheduler.getPlayingTrack().ifPresent(track -> trackList.add(0, track));
                    return trackList.stream().map(track -> track.getInfo().uri).collect(Collectors.toList());
                })
                .collectList()
                .flatMap(tracks -> Mono.justOrEmpty(event.getMember())
                        .map(member -> member.getId().asLong())
                        .map(id -> new Playlist(id, tracks))
                )
                .flatMap(playlistService::save)
                .doOnError(throwable -> logger.error("Failed to save playlist", throwable))
                .flatMap(playlist -> createSavePlaylistSuccessMessage(event))
                .switchIfEmpty(createFailedSaveErrorMessage(event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<TextMessage> createFailedSaveErrorMessage(SavePlaylistEvent event){
        return event.getMessage().getChannel()
                .zipWith(Mono.justOrEmpty(event.getMember().flatMap(Member::getNickname)))
                .map(tuple -> new TextMessage(tuple.getT1(), String.format("**%s**, could not save your playlist, try again later!",
                        tuple.getT2()), false));
    }

    private Mono<TextMessage> createSavePlaylistSuccessMessage(SavePlaylistEvent event){
        return event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel, "Saved new playlist!", false));
    }
}
