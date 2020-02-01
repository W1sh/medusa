package com.w1sh.medusa.listeners.playlists;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.TrackScheduler;
import com.w1sh.medusa.data.events.EventFactory;
import com.w1sh.medusa.data.responses.Embed;
import com.w1sh.medusa.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.playlists.LoadPlaylistEvent;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.data.Playlist;
import com.w1sh.medusa.services.PlaylistService;
import com.w1sh.medusa.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
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
                .flatMapIterable(Playlist::getTracks)
                .flatMap(track -> event.getGuild()
                        .map(guild -> guild.getId().asLong())
                        .flatMap(id -> AudioConnectionManager.getInstance().requestTrack(id, track.getUri())))
                .last()
                .flatMap(trackScheduler -> createEmbed(trackScheduler, event))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> logger.error("Failed to load playlist", throwable)))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<Embed> createEmbed(TrackScheduler trackScheduler, LoadPlaylistEvent event){
        return event.getMessage().getChannel()
                .map(channel -> new Embed(channel, embedCreateSpec -> {
                    embedCreateSpec.setColor(Color.GREEN);
                    embedCreateSpec.setTitle("Loaded playlist");
                    embedCreateSpec.setDescription(String.format("**%d** tracks loaded | %s",
                            trackScheduler.getFullQueue().size(),
                            ResponseUtils.formatDuration(trackScheduler.getQueueDuration())));
                }));
    }
}
