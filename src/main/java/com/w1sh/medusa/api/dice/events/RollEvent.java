package com.w1sh.medusa.api.dice.events;

import com.w1sh.medusa.api.MultipleArgumentsEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class RollEvent extends MultipleArgumentsEvent {

    public static final String KEYWORD = "roll";
    private static final Integer NUM_ALLOWED_ARGS = 2;

    public RollEvent(MessageCreateEvent event) {
        super(event, NUM_ALLOWED_ARGS);
    }

}
