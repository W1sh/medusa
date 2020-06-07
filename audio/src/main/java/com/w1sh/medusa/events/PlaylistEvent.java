package com.w1sh.medusa.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Registered;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Registered(prefix = "playlist")
public final class PlaylistEvent extends Event {

    public PlaylistEvent(MessageCreateEvent event) {
        super(event);
    }
}
