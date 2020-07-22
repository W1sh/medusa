package com.w1sh.medusa.api.misc.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Type(prefix = "clapify")
public final class ClapifyEvent extends Event {

    public ClapifyEvent(MessageCreateEvent event) {
        super(event, 1);
    }
}
