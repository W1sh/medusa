package com.w1sh.medusa.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Registered;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Registered(prefix = "clear")
public final class ClearQueueEvent extends Event {

    public ClearQueueEvent(MessageCreateEvent event) {
        super(event);
    }

}
