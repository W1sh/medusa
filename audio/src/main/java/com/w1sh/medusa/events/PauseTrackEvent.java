package com.w1sh.medusa.events;

import com.w1sh.medusa.data.events.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

public final class PauseTrackEvent extends Event {

    public static final String KEYWORD = "pause";

    public PauseTrackEvent(MessageCreateEvent event) {
        super(event);
    }

}
