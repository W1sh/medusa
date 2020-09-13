package com.w1sh.medusa.listeners.playlists;

import com.w1sh.medusa.data.Playlist;
import com.w1sh.medusa.events.PlaylistEvent;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.services.PlaylistService;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public final class PlaylistShowAction implements Function<PlaylistEvent, Mono<Message>> {

    private final PlaylistService playlistService;
    private final MessageService messageService;

    @Override
    public Mono<Message> apply(PlaylistEvent event) {
        return playlistService.findAllByUserId(event.getUserId())
                .flatMap(playlists -> messageService.send(event.getChannel(), createPlaylistsEmbedSpec(playlists, event)))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> log.error("Failed to list all playlists of user", throwable)));
    }

    private Consumer<EmbedCreateSpec> createPlaylistsEmbedSpec(List<Playlist> playlists, PlaylistEvent event){
        return embedCreateSpec -> {
            embedCreateSpec.setColor(Color.GREEN);
            embedCreateSpec.setTitle(String.format("%s saved playlists",
                    event.getNickname()));
            for (Playlist playlist : playlists) {
                embedCreateSpec.addField(String.format("**%s**", playlist.getName()), String.format("** %s %d track(s)** | %s",
                        MessageService.BULLET,
                        playlist.getTracks() != null ? playlist.getTracks().size() : 0,
                        MessageService.formatDuration(playlist.getFullDuration())), false);
            }
        };
    }
}
