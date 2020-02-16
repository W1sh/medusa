package com.w1sh.medusa.events.playlists;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Registered;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Registered(prefix = "playlists")
public final class PlaylistsEvent extends Event {

    public static final String KEYWORD = "playlists";

    public PlaylistsEvent(MessageCreateEvent event) {
        super(event);
    }
}
