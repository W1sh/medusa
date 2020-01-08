package com.w1sh.medusa.events;

import com.w1sh.medusa.core.events.InlineEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class CardImageEvent extends InlineEvent {

    public static final String INLINE_PREFIX = "{{!";

    public CardImageEvent(MessageCreateEvent event) {
        super(event, INLINE_PREFIX);
    }
}
