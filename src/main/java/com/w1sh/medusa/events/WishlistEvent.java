package com.w1sh.medusa.events;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.events.EventType;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Type(prefix = "wishlist", eventType = EventType.MTG)
public final class WishlistEvent extends Event {

    public WishlistEvent(MessageCreateEvent event) {
        super(event);
    }
}
