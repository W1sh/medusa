package com.w1sh.medusa.events;

import com.w1sh.medusa.data.events.InlineEvent;
import com.w1sh.medusa.data.events.Registered;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Registered(prefix = "{{")
public final class CardDetailEvent extends InlineEvent {

    public static final String INLINE_PREFIX = "{{";

    public CardDetailEvent(MessageCreateEvent event) {
        super(event, INLINE_PREFIX);
    }
}
