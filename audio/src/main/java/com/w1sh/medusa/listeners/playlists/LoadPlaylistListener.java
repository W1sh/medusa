package com.w1sh.medusa.listeners.playlists;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.data.Track;
import com.w1sh.medusa.data.responses.Embed;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.playlists.LoadPlaylistEvent;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.services.PlaylistService;
import com.w1sh.medusa.services.TrackService;
import com.w1sh.medusa.utils.ResponseUtils;
import discord4j.common.util.Snowflake;
import discord4j.rest.util.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Component
public final class LoadPlaylistListener implements EventListener<LoadPlaylistEvent> {

    private static final Logger logger = LoggerFactory.getLogger(LoadPlaylistListener.class);

    private final PlaylistService playlistService;
    private final TrackService trackService;
    private final ResponseDispatcher responseDispatcher;
    private final AudioConnectionManager audioConnectionManager;

    public LoadPlaylistListener(PlaylistService playlistService, TrackService trackService,
                                ResponseDispatcher responseDispatcher, AudioConnectionManager audioConnectionManager) {
        this.playlistService = playlistService;
        this.trackService = trackService;
        this.responseDispatcher = responseDispatcher;
        this.audioConnectionManager = audioConnectionManager;
    }

    @Override
    public Class<LoadPlaylistEvent> getEventType() {
        return LoadPlaylistEvent.class;
    }

    @Override
    public Mono<Void> execute(LoadPlaylistEvent event) {
        Integer playlistId = Optional.of(event.getMessage().getContent()).map(c -> Integer.parseInt(c.split(" ")[1])).orElse(1);
        String userId = event.getMember().map(member -> member.getId().asString()).orElse("");
        Snowflake guildId = event.getGuildId().orElse(Snowflake.of(0L));

        return playlistService.findAllByUserId(userId)
                .flatMapIterable(Function.identity())
                .take(playlistId)
                .last()
                .flatMap(trackService::findAllByPlaylistId)
                .flatMapMany(Flux::fromIterable)
                .doOnNext(track -> audioConnectionManager.requestTrack(guildId.asLong(), track.getUri()))
                .collectList()
                .flatMap(tracks -> createEmbed(tracks, event))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> logger.error("Failed to load playlist", throwable)))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<Embed> createEmbed(List<Track> tracks, LoadPlaylistEvent event){
        Long duration = tracks.stream().map(Track::getDuration).reduce(Long::sum).orElse(0L);

        return event.getMessage().getChannel()
                .map(channel -> new Embed(channel, embedCreateSpec -> {
                    embedCreateSpec.setColor(Color.GREEN);
                    embedCreateSpec.setTitle("Loaded playlist");
                    embedCreateSpec.setDescription(String.format("**%d** tracks loaded | %s",
                            tracks.size(), ResponseUtils.formatDuration(duration)));
                }));
    }
}
