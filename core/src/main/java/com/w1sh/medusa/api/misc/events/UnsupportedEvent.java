package com.w1sh.medusa.api.misc.events;

import com.w1sh.medusa.core.events.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class UnsupportedEvent extends Event {

    public UnsupportedEvent(MessageCreateEvent event) {
        super(event);
    }

}
