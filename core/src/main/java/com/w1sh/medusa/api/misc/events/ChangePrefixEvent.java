package com.w1sh.medusa.api.misc.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Type(prefix = "cprefix")
public final class ChangePrefixEvent extends Event {

    private static final Integer NUM_ALLOWED_ARGS = 1;

    public ChangePrefixEvent(MessageCreateEvent event) {
        super(event, NUM_ALLOWED_ARGS);
    }
}
