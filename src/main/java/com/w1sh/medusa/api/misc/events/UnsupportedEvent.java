package com.w1sh.medusa.api.misc.events;

import com.w1sh.medusa.api.CommandEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class UnsupportedEvent extends CommandEvent {

    public UnsupportedEvent(MessageCreateEvent event) {
        super(event);
    }

}
