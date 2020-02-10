package com.w1sh.medusa.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Registered;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Registered(prefix = "play")
public final class PlayTrackEvent extends Event {

    public static final String KEYWORD = "play";

    public PlayTrackEvent(MessageCreateEvent event) {
        super(event, 1);
    }

}
