package com.w1sh.medusa.api.misc.events;

import com.w1sh.medusa.core.events.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class StatusEvent extends Event {

    public static final String KEYWORD = "status";

    public StatusEvent(MessageCreateEvent event) {
        super(event);
    }
}
