package com.w1sh.medusa.events;

import com.w1sh.medusa.core.events.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class StopTrackEvent extends Event {

    public static final String KEYWORD = "stop";

    public StopTrackEvent(MessageCreateEvent event) {
        super(event);
    }

}
