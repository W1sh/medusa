package com.w1sh.medusa.api.audio.events;

import com.w1sh.medusa.api.SingleArgumentEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class SkipTrackEvent extends SingleArgumentEvent {

    public static final String KEYWORD = "skip";

    public SkipTrackEvent(MessageCreateEvent event) {
        super(event);
    }
}
