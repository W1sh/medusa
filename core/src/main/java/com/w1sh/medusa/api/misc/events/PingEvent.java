package com.w1sh.medusa.api.misc.events;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Type(prefix = "ping")
public final class PingEvent extends Event {

    public PingEvent(MessageCreateEvent event) {
        super(event);
    }

}
