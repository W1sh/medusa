package com.w1sh.medusa.events;

import com.w1sh.medusa.data.events.EventType;
import com.w1sh.medusa.data.events.InlineEvent;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Type(prefix = "{{@", eventType = EventType.MTG)
public final class CardArtworkEvent extends InlineEvent {

    public CardArtworkEvent(MessageCreateEvent event) {
        super(event);
    }

}
