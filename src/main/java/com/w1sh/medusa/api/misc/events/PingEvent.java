package com.w1sh.medusa.api.misc.events;

import com.w1sh.medusa.api.SingleArgumentEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class PingEvent extends SingleArgumentEvent {

    public static final String KEYWORD = "ping";

    public PingEvent(MessageCreateEvent event) {
        super(event);
    }

}
