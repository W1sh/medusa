package com.w1sh.medusa.listeners.playlists;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.data.Track;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.events.PlaylistEvent;
import com.w1sh.medusa.services.PlaylistService;
import com.w1sh.medusa.services.TrackService;
import com.w1sh.medusa.utils.ResponseUtils;
import discord4j.common.util.Snowflake;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public final class PlaylistLoadAction implements Function<PlaylistEvent, Mono<? extends Response>> {

    private final PlaylistService playlistService;
    private final TrackService trackService;
    private final AudioConnectionManager audioConnectionManager;

    @Override
    public Mono<? extends Response> apply(PlaylistEvent event) {
        int index = Integer.parseInt(event.getArguments().get(1));
        String userId = event.getMember().map(member -> member.getId().asString()).orElse("");
        Snowflake guildId = event.getGuildId().orElse(Snowflake.of(0L));

        return playlistService.findAllByUserId(userId)
                .flatMapIterable(Function.identity())
                .take(index)
                .last()
                .flatMap(trackService::findAllByPlaylistId)
                .flatMapMany(Flux::fromIterable)
                .doOnNext(track -> audioConnectionManager.requestTrack(guildId.asLong(), track.getUri()))
                .collectList()
                .flatMap(tracks -> createEmbed(tracks, event))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> log.error("Failed to load playlist", throwable)));
    }

    private Mono<TextMessage> createEmbed(List<Track> tracks, PlaylistEvent event){
        Long duration = tracks.stream().map(Track::getDuration).reduce(Long::sum).orElse(0L);

        return event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel, String.format("Loaded playlist with **%d** tracks loaded and a total duration of **%s**",
                        tracks.size(), ResponseUtils.formatDuration(duration)), false));
    }
}
