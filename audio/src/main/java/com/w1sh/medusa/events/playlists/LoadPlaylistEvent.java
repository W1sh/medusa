package com.w1sh.medusa.events.playlists;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Registered;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Registered(prefix = "loadplaylist")
public final class LoadPlaylistEvent extends Event {

    public static final String KEYWORD = "loadplaylist";

    public LoadPlaylistEvent(MessageCreateEvent event) {
        super(event, 1);
    }
}
