package com.w1sh.medusa.api.dice.events;

import com.w1sh.medusa.api.CommandEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class DuelRollEvent extends CommandEvent {

    public static final String KEYWORD = "droll";

    public DuelRollEvent(MessageCreateEvent event) {
        super(event);
    }

}
