package com.w1sh.medusa.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Registered;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Registered(prefix = "pause")
public final class PauseTrackEvent extends Event {

    public PauseTrackEvent(MessageCreateEvent event) {
        super(event);
    }

}
