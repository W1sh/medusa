package com.w1sh.medusa.events.playlists;

import com.w1sh.medusa.core.events.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

public final class LoadPlaylistEvent extends Event {

    public static final String KEYWORD = "loadplaylist";

    public LoadPlaylistEvent(MessageCreateEvent event) {
        super(event, 2);
    }
}
