package com.w1sh.medusa.api.misc.events;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.events.EventType;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Type(prefix = "disable", minimumArguments = 2, eventType = EventType.OTHER)
public final class DisableEvent extends Event {

    public DisableEvent(MessageCreateEvent event) {
        super(event);
    }
}
