package com.w1sh.medusa.events;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.events.EventType;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Type(prefix = "points", eventType = EventType.GAMBLING)
public final class PointsEvent extends Event {

    public PointsEvent(MessageCreateEvent event) {
        super(event);
    }
}
