package com.w1sh.medusa.listeners.playlists;

import com.w1sh.medusa.data.Playlist;
import com.w1sh.medusa.data.responses.Embed;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.events.PlaylistEvent;
import com.w1sh.medusa.services.PlaylistService;
import com.w1sh.medusa.utils.ResponseUtils;
import discord4j.core.object.entity.Member;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public final class PlaylistShowAction implements Function<PlaylistEvent, Mono<? extends Response>> {

    private final PlaylistService playlistService;

    @Override
    public Mono<? extends Response> apply(PlaylistEvent event) {
        String userId = event.getMember().map(member -> member.getId().asString()).orElse("");

        return playlistService.findAllByUserId(userId)
                .flatMap(playlists -> createPlaylistsEmbed(playlists, event))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> log.error("Failed to list all playlists of user", throwable)));
    }

    private Mono<Embed> createPlaylistsEmbed(List<Playlist> playlists, PlaylistEvent event){
        return event.getMessage().getChannel()
                .map(channel -> new Embed(channel, embedCreateSpec -> {
                    embedCreateSpec.setColor(Color.GREEN);
                    embedCreateSpec.setTitle(String.format("%s saved playlists",
                            event.getMember().flatMap(Member::getNickname).orElse("")));
                    for (Playlist playlist : playlists) {
                        embedCreateSpec.addField(String.format("**%s**", playlist.getName()), String.format("** %s %d track(s)** | %s",
                                ResponseUtils.BULLET,
                                playlist.getTracks() != null ? playlist.getTracks().size() : 0,
                                ResponseUtils.formatDuration(playlist.getFullDuration())), false);
                    }
                }));
    }
}
