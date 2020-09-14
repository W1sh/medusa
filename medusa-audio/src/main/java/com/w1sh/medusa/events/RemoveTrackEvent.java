package com.w1sh.medusa.events;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Type(prefix = "remove", minimumArguments = 1)
public class RemoveTrackEvent extends Event {

    public RemoveTrackEvent(MessageCreateEvent event) {
        super(event);
    }
}
