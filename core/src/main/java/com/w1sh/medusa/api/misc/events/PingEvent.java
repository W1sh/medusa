package com.w1sh.medusa.api.misc.events;

import com.w1sh.medusa.data.events.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class PingEvent extends Event {

    public static final String KEYWORD = "ping";

    public PingEvent(MessageCreateEvent event) {
        super(event);
    }

}
