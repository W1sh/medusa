package com.w1sh.medusa.events;

import com.w1sh.medusa.data.events.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

public final class ShuffleQueueEvent extends Event {

    public static final String KEYWORD = "shuffle";

    public ShuffleQueueEvent(MessageCreateEvent event) {
        super(event);
    }
}
