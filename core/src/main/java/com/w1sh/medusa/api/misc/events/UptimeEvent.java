package com.w1sh.medusa.api.misc.events;

import com.w1sh.medusa.core.events.SingleArgumentEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class UptimeEvent extends SingleArgumentEvent {

    public static final String KEYWORD = "uptime";

    public UptimeEvent(MessageCreateEvent event) {
        super(event);
    }
}
