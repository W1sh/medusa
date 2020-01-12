package com.w1sh.medusa.events;

import com.w1sh.medusa.core.events.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class PauseTrackEvent extends Event {

    public static final String KEYWORD = "pause";

    public PauseTrackEvent(MessageCreateEvent event) {
        super(event);
    }

}
