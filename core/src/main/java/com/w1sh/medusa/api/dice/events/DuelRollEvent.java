package com.w1sh.medusa.api.dice.events;

import com.w1sh.medusa.data.events.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

public final class DuelRollEvent extends Event {

    public static final String KEYWORD = "duelroll";
    private static final Integer NUM_ALLOWED_ARGS = 2;

    public DuelRollEvent(MessageCreateEvent event) {
        super(event, NUM_ALLOWED_ARGS);
    }

}
