package com.w1sh.medusa.api.misc.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Registered;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Registered(prefix = "ping")
public final class PingEvent extends Event {

    public PingEvent(MessageCreateEvent event) {
        super(event);
    }

}
