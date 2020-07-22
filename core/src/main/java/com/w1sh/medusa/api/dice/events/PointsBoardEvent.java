package com.w1sh.medusa.api.dice.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.EventType;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Type(prefix = "pointsboard", eventType = EventType.GAMBLING)
public final class PointsBoardEvent extends Event {

    public PointsBoardEvent(MessageCreateEvent event) {
        super(event);
    }
}
