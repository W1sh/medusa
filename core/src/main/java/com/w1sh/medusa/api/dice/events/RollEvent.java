package com.w1sh.medusa.api.dice.events;

import com.w1sh.medusa.core.events.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class RollEvent extends Event {

    public static final String KEYWORD = "roll";
    private static final Integer NUM_ALLOWED_ARGS = 1;

    public RollEvent(MessageCreateEvent event) {
        super(event, NUM_ALLOWED_ARGS);
    }

}
