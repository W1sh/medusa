package com.w1sh.medusa.events;

import com.w1sh.medusa.data.events.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

public final class QueueTrackEvent extends Event {

    public static final String KEYWORD = "queue";

    public QueueTrackEvent(MessageCreateEvent event) {
        super(event);
    }
}
