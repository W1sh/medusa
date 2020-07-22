package com.w1sh.medusa.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Type(prefix = "remove")
public class RemoveTrackEvent extends Event {

    private static final Integer NUM_ALLOWED_ARGS = 1;

    public RemoveTrackEvent(MessageCreateEvent event) {
        super(event, NUM_ALLOWED_ARGS);
    }
}
