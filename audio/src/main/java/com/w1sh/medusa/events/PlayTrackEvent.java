package com.w1sh.medusa.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Type(prefix = "play")
public final class PlayTrackEvent extends Event {

    public PlayTrackEvent(MessageCreateEvent event) {
        super(event, 1);
    }

}
