package com.w1sh.medusa.events;

import com.w1sh.medusa.data.events.InlineEvent;
import com.w1sh.medusa.data.events.Registered;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Registered(prefix = "{{?")
public final class CardSearchEvent extends InlineEvent {

    public CardSearchEvent(MessageCreateEvent event) {
        super(event);
    }
}
