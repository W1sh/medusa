package com.w1sh.medusa.api.misc.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.EventType;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Type(prefix = "disable", eventType = EventType.OTHER)
public final class DisableEvent extends Event {

    public DisableEvent(MessageCreateEvent event) {
        super(event, 2);
    }
}
