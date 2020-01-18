package com.w1sh.medusa.listeners;

import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.events.PlaylistsEvent;
import com.w1sh.medusa.mongo.services.PlaylistService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

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
        return null;
    }
}
