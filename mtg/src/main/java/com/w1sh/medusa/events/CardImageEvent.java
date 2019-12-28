package com.w1sh.medusa.events;

import com.w1sh.medusa.core.events.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class CardImageEvent extends Event {

    public static final String INLINE_PREFIX = "{{!";

    public CardImageEvent(MessageCreateEvent event) {
        super(event, true, INLINE_PREFIX);
    }
}
