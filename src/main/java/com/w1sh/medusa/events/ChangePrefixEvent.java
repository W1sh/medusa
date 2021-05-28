package com.w1sh.medusa.events;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Type(prefix = "cprefix", minimumArguments = 1)
public final class ChangePrefixEvent extends Event {

    public ChangePrefixEvent(MessageCreateEvent event) {
        super(event);
    }
}
