package com.w1sh.medusa.events.playlists;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Registered;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Registered(prefix = "saveplaylist")
public final class SavePlaylistEvent extends Event {

    public static final String KEYWORD = "saveplaylist";

    public SavePlaylistEvent(MessageCreateEvent event) {
        super(event);
    }
}
