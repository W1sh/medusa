package com.w1sh.medusa.events.playlists;

import com.w1sh.medusa.core.events.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

public final class PlaylistsEvent extends Event {

    public static final String KEYWORD = "playlists";

    public PlaylistsEvent(MessageCreateEvent event) {
        super(event);
    }
}
