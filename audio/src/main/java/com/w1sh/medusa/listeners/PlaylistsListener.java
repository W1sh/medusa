package com.w1sh.medusa.listeners;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.w1sh.medusa.core.data.Embed;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.events.PlaylistsEvent;
import com.w1sh.medusa.mongo.entities.Playlist;
import com.w1sh.medusa.mongo.services.PlaylistService;
import com.w1sh.medusa.utils.Messenger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.util.List;

@Component
public class PlaylistsListener implements EventListener<PlaylistsEvent> {

    private final PlaylistService playlistService;
    private final ResponseDispatcher responseDispatcher;

    public PlaylistsListener(ResponseDispatcher responseDispatcher, CommandEventDispatcher eventDispatcher, PlaylistService playlistService) {
        this.responseDispatcher = responseDispatcher;
        this.playlistService = playlistService;
        EventFactory.registerEvent(PlaylistsEvent.KEYWORD, PlaylistsEvent.class);
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<PlaylistsEvent> getEventType() {
        return PlaylistsEvent.class;
    }

    @Override
    public Mono<Void> execute(PlaylistsEvent event) {
        return Mono.justOrEmpty(event.getMember())
                .map(member -> member.getId().asLong())
                .flatMap(id -> playlistService.findAllByUserId(id)
                        .collectList())
                .flatMap(playlists -> createEmbed(playlists, event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<Embed> createEmbed(List<Playlist> playlists, PlaylistsEvent event){
        return event.getMessage().getChannel()
                .map(channel -> new Embed(channel, embedCreateSpec -> {
                    embedCreateSpec.setColor(Color.GREEN);
                    embedCreateSpec.setTitle("Saved playlists");
                    for (Playlist playlist : playlists) {
                            embedCreateSpec.addField("Playlist", String.format("**%d track(s)**",
                                    playlist.getTracks().size()), false);
                    }
                }));
    }
}
