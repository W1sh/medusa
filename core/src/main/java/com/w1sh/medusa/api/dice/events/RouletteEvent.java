package com.w1sh.medusa.api.dice.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.EventType;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Type(prefix = "roulette", eventType = EventType.GAMBLING)
public final class RouletteEvent extends Event {

    private static final Integer NUM_ALLOWED_ARGS = 1;

    public RouletteEvent(MessageCreateEvent event) {
        super(event, NUM_ALLOWED_ARGS);
    }
}
