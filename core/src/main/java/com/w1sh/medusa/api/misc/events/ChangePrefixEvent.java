package com.w1sh.medusa.api.misc.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Registered;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Registered(prefix = "cprefix")
public final class ChangePrefixEvent extends Event {

    public static final String KEYWORD = "cprefix";
    private static final Integer NUM_ALLOWED_ARGS = 1;

    public ChangePrefixEvent(MessageCreateEvent event) {
        super(event, NUM_ALLOWED_ARGS);
    }
}
