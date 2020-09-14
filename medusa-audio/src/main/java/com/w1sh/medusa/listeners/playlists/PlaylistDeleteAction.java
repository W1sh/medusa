package com.w1sh.medusa.listeners.playlists;

import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.events.PlaylistEvent;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.services.PlaylistService;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public final class PlaylistDeleteAction implements Function<PlaylistEvent, Mono<Message>> {

    private static final String DELETE_ALL_PARAMETER = "all";

    private final PlaylistService playlistService;
    private final MessageService messageService;

    @Override
    public Mono<Message> apply(PlaylistEvent event) {
        if (DELETE_ALL_PARAMETER.equalsIgnoreCase(event.getArguments().get(1))) return deleteAll(event);

        final Integer index = Integer.parseInt(event.getArguments().get(1));

        return playlistService.deleteIndex(event.getUserId(), index)
                .flatMap(ignored -> messageService.send(event.getChannel(), MessageEnum.PLAYLIST_DELETE_SUCCESS))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> log.error("Failed to delete playlist", throwable)));
    }

    private Mono<Message> deleteAll(PlaylistEvent event) {
        return playlistService.deleteByUserId(event.getUserId())
                .flatMap(ignored -> messageService.send(event.getChannel(), MessageEnum.PLAYLIST_DELETEALL_SUCCESS))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> log.error("Failed to delete all playlists of user with id <{}>",
                        event.getGuildId(), throwable)));
    }

}
