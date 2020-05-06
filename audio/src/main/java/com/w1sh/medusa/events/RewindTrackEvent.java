package com.w1sh.medusa.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Registered;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Registered(prefix = "rwd")
public final class RewindTrackEvent extends Event {

    public RewindTrackEvent(MessageCreateEvent event) {
        super(event);
    }
}
