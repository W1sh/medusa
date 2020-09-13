package com.w1sh.medusa.listeners.playlists;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.data.Playlist;
import com.w1sh.medusa.data.Track;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.events.PlaylistEvent;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.services.PlaylistService;
import com.w1sh.medusa.utils.ResponseUtils;
import discord4j.core.object.entity.Message;
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
public final class PlaylistLoadAction implements Function<PlaylistEvent, Mono<Message>> {

    private final PlaylistService playlistService;
    private final AudioConnectionManager audioConnectionManager;
    private final MessageService messageService;

    @Override
    public Mono<Message> apply(PlaylistEvent event) {
        final int index = Integer.parseInt(event.getArguments().get(1));

        return playlistService.findAllByUserId(event.getUserId())
                .flatMapIterable(Function.identity())
                .take(index)
                .last()
                .map(Playlist::getTracks)
                .flatMapMany(Flux::fromIterable)
                .doOnNext(track -> audioConnectionManager.requestTrack(event.getGuildId(), track.getUri()))
                .collectList()
                .flatMap(tracks -> createLoadSuccessMessage(tracks, event))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> log.error("Failed to load playlist", throwable)));
    }

    private Mono<Message> createLoadSuccessMessage(List<Track> tracks, PlaylistEvent event){
        final Long duration = tracks.stream().map(Track::getDuration).reduce(Long::sum).orElse(0L);

        return messageService.send(event.getChannel(), MessageEnum.PLAYLIST_LOAD_SUCCESS,
                String.valueOf(tracks.size()), ResponseUtils.formatDuration(duration));
    }
}
