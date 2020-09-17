package com.w1sh.medusa.events;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Type(prefix = "purge")
public final class PurgeEvent extends Event {

    public PurgeEvent(MessageCreateEvent event) {
        super(event);
    }
}
