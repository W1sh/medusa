package com.w1sh.medusa.listeners.playlists;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.core.data.Embed;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.events.playlists.LoadPlaylistEvent;
import com.w1sh.medusa.mongo.entities.Playlist;
import com.w1sh.medusa.mongo.services.PlaylistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.*;

@Component
public final class LoadPlaylistListener implements EventListener<LoadPlaylistEvent> {

    private static final Logger logger = LoggerFactory.getLogger(LoadPlaylistEvent.class);

    private final PlaylistService playlistService;
    private final ResponseDispatcher responseDispatcher;

    public LoadPlaylistListener(PlaylistService playlistService, CommandEventDispatcher eventDispatcher, ResponseDispatcher responseDispatcher) {
        this.playlistService = playlistService;
        this.responseDispatcher = responseDispatcher;
        EventFactory.registerEvent(LoadPlaylistEvent.KEYWORD, LoadPlaylistEvent.class);
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<LoadPlaylistEvent> getEventType() {
        return LoadPlaylistEvent.class;
    }

    @Override
    public Mono<Void> execute(LoadPlaylistEvent event) {
        return Mono.justOrEmpty(event.getMember())
                .map(member -> member.getId().asLong())
                .flatMapMany(playlistService::findAllByUserId)
                .take(event.getMessage().getContent().map(c -> Integer.parseInt(c.split(" ")[1])).orElse(1))
                .last()
                .flatMap(playlist -> Flux.fromIterable(playlist.getTracks())
                        .flatMap(link -> event.getGuild()
                                .map(guild -> guild.getId().asLong())
                                .flatMap(id -> AudioConnectionManager.getInstance().requestTrack(id, link.getUri())))
                        .last()
                        .map(t -> playlist))
                .flatMap(playlist -> createEmbed(playlist, event))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> logger.error("Failed to load playlist", throwable)))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<Embed> createEmbed(Playlist playlist, LoadPlaylistEvent event){
        return event.getMessage().getChannel()
                .map(channel -> new Embed(channel, embedCreateSpec -> {
                    embedCreateSpec.setColor(Color.GREEN);
                    embedCreateSpec.setTitle("Loaded playlist");
                    embedCreateSpec.setDescription(String.format("**%d** tracks", playlist.getTracks().size()));
                }));
    }
}
