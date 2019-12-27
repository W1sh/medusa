package com.w1sh.medusa.events;

import com.w1sh.medusa.core.events.MultipleArgumentsEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class CardSearchEvent extends MultipleArgumentsEvent {

    public static final String INLINE_PREFIX = "{{";
    public static final String KEYWORD = "mcardsearch";

    public CardSearchEvent(MessageCreateEvent event) {
        super(event, 2);
    }
}
