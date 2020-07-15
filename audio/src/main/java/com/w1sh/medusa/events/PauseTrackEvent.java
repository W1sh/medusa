package com.w1sh.medusa.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Type(prefix = "pause")
public final class PauseTrackEvent extends Event {

    public PauseTrackEvent(MessageCreateEvent event) {
        super(event);
    }

}
