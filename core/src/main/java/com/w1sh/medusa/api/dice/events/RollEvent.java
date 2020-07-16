package com.w1sh.medusa.api.dice.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.EventType;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Type(prefix = "roll", eventType = EventType.GAMBLING)
public final class RollEvent extends Event {

    private static final Integer NUM_ALLOWED_ARGS = 1;

    public RollEvent(MessageCreateEvent event) {
        super(event, NUM_ALLOWED_ARGS);
    }

}
