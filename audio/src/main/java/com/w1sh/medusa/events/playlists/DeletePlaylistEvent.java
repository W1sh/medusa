package com.w1sh.medusa.events.playlists;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Registered;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Registered(prefix = "deleteplaylist")
public final class DeletePlaylistEvent extends Event {

    public DeletePlaylistEvent(MessageCreateEvent event) {
        super(event);
    }
}
