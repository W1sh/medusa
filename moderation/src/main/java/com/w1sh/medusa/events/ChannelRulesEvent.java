package com.w1sh.medusa.events;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.events.EventType;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Type(prefix = "crules", eventType = EventType.MODERATION)
public final class ChannelRulesEvent extends Event {

    public ChannelRulesEvent(MessageCreateEvent event) {
        super(event);
    }
}
