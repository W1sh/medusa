package com.w1sh.medusa.events.playlists;

import com.w1sh.medusa.data.events.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

public final class DeletePlaylistEvent extends Event {

    public static final String KEYWORD = "deleteplaylist";

    public DeletePlaylistEvent(MessageCreateEvent event) {
        super(event);
    }
}
