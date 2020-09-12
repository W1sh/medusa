package com.w1sh.medusa.api.dice.events;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.events.EventType;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Type(prefix = "roll", minimumArguments = 1, eventType = EventType.GAMBLING)
public final class RollEvent extends Event {

    public RollEvent(MessageCreateEvent event) {
        super(event);
    }
}
