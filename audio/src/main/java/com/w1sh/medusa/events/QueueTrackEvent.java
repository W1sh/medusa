package com.w1sh.medusa.events;

import com.w1sh.medusa.core.events.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class QueueTrackEvent extends Event {

    public static final String KEYWORD = "queue";

    public QueueTrackEvent(MessageCreateEvent event) {
        super(event);
    }
}
