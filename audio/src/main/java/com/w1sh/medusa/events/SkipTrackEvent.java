package com.w1sh.medusa.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Registered;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Registered(prefix = "skip")
public final class SkipTrackEvent extends Event {

    public static final String KEYWORD = "skip";

    public SkipTrackEvent(MessageCreateEvent event) {
        super(event);
    }
}
