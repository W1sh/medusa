package com.w1sh.medusa.api.misc.events;

import com.w1sh.medusa.core.events.CommandEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class PingEvent extends CommandEvent {

    public PingEvent(MessageCreateEvent event) {
        super(event);
    }

}
