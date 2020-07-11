package com.w1sh.medusa.api.moderation.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Registered;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Registered(prefix = "crules")
public final class ChannelRulesEvent extends Event {

    public ChannelRulesEvent(MessageCreateEvent event) {
        super(event);
    }
}
