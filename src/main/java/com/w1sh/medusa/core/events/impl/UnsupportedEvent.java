package com.w1sh.medusa.core.events.impl;

import com.w1sh.medusa.core.events.CommandEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class UnsupportedEvent extends CommandEvent {

    public UnsupportedEvent(MessageCreateEvent event) {
        super(event);
    }

}
