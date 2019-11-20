package com.w1sh.medusa.api.audio.events;

import com.w1sh.medusa.api.SingleArgumentEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class StopTrackEvent extends SingleArgumentEvent {

    public static final String KEYWORD = "stop";

    public StopTrackEvent(MessageCreateEvent event) {
        super(event);
    }

}
