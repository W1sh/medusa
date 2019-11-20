package com.w1sh.medusa.api.audio.events;

import com.w1sh.medusa.api.SingleArgumentEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class NextTrackEvent extends SingleArgumentEvent {

    public static final String KEYWORD = "next";

    public NextTrackEvent(MessageCreateEvent event) {
        super(event);
    }

}
