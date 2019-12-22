package com.w1sh.medusa.api.misc.events;

import com.w1sh.medusa.core.events.MultipleArgumentsEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class ChangePrefixEvent extends MultipleArgumentsEvent {

    public static final String KEYWORD = "cprefix";
    private static final Integer NUM_ALLOWED_ARGS = 2;

    public ChangePrefixEvent(MessageCreateEvent event) {
        super(event, NUM_ALLOWED_ARGS);
    }
}
