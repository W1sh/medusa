package com.w1sh.medusa.events;

import com.w1sh.medusa.core.events.SingleArgumentEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class PauseTrackEvent extends SingleArgumentEvent {

    public static final String KEYWORD = "pause";

    public PauseTrackEvent(MessageCreateEvent event) {
        super(event);
    }

}
