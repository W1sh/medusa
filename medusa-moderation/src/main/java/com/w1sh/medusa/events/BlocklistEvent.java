package com.w1sh.medusa.events;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Type(prefix = "blocklist")
public final class BlocklistEvent extends Event {

    public BlocklistEvent(MessageCreateEvent event) {
        super(event);
    }
}
