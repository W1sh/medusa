package com.w1sh.medusa.api.misc.events;

import com.w1sh.medusa.core.events.MultipleArgumentsEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class ClapifyEvent extends MultipleArgumentsEvent {

    public static final String KEYWORD = "clapify";

    public ClapifyEvent(MessageCreateEvent event) {
        super(event, Integer.MAX_VALUE);
    }
}
