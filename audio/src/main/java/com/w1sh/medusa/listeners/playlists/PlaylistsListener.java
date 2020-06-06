package com.w1sh.medusa.listeners.playlists;

import com.w1sh.medusa.data.Playlist;
import com.w1sh.medusa.data.responses.Embed;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.playlists.PlaylistsEvent;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.services.PlaylistService;
import com.w1sh.medusa.utils.ResponseUtils;
import discord4j.core.object.entity.Member;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public final class PlaylistsListener implements EventListener<PlaylistsEvent> {

    private final PlaylistService playlistService;
    private final ResponseDispatcher responseDispatcher;

    @Override
    public Class<PlaylistsEvent> getEventType() {
        return PlaylistsEvent.class;
    }

    @Override
    public Mono<Void> execute(PlaylistsEvent event) {
        String userId = event.getMember().map(member -> member.getId().asString()).orElse("");

        return playlistService.findAllByUserId(userId)
                .flatMap(playlists -> createEmbed(playlists, event))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> log.error("Failed to list all playlists of user", throwable)))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<Embed> createEmbed(List<Playlist> playlists, PlaylistsEvent event){
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
