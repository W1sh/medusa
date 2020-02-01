package com.w1sh.medusa.data.events;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class UnsupportedEvent extends Event {

    public UnsupportedEvent(MessageCreateEvent event) {
        super(event);
    }

}
