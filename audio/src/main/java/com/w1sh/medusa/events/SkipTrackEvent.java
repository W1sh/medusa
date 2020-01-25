package com.w1sh.medusa.events;

import com.w1sh.medusa.core.events.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

public final class SkipTrackEvent extends Event {

    public static final String KEYWORD = "skip";

    public SkipTrackEvent(MessageCreateEvent event) {
        super(event);
    }
}
