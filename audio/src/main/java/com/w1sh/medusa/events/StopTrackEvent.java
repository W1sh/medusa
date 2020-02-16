package com.w1sh.medusa.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Registered;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Registered(prefix = "stop")
public final class StopTrackEvent extends Event {

    public static final String KEYWORD = "stop";

    public StopTrackEvent(MessageCreateEvent event) {
        super(event);
    }

}
