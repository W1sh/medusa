package com.w1sh.medusa.events;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Type(prefix = "clear")
public final class ClearQueueEvent extends Event {

    public ClearQueueEvent(MessageCreateEvent event) {
        super(event);
    }

}
