package com.w1sh.medusa.api.dice.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Registered;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Registered(prefix = "roulette")
public final class RouletteEvent extends Event {

    private static final Integer NUM_ALLOWED_ARGS = 1;

    public RouletteEvent(MessageCreateEvent event) {
        super(event, NUM_ALLOWED_ARGS);
    }
}
