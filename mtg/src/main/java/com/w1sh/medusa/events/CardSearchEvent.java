package com.w1sh.medusa.events;

import com.w1sh.medusa.core.events.InlineEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class CardSearchEvent extends InlineEvent {

    public static final String INLINE_PREFIX = "{{";

    public CardSearchEvent(MessageCreateEvent event) {
        super(event, INLINE_PREFIX);
    }
}
