package com.w1sh.medusa.api.misc.events;

import com.w1sh.medusa.core.events.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class ChangePrefixEvent extends Event {

    public static final String KEYWORD = "cprefix";
    private static final Integer NUM_ALLOWED_ARGS = 1;

    public ChangePrefixEvent(MessageCreateEvent event) {
        super(event, NUM_ALLOWED_ARGS);
    }
}
