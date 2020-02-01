package com.w1sh.medusa.api.misc.events;

import com.w1sh.medusa.data.events.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class UptimeEvent extends Event {

    public static final String KEYWORD = "uptime";

    public UptimeEvent(MessageCreateEvent event) {
        super(event);
    }
}
