package com.w1sh.medusa.events;

import com.w1sh.medusa.core.events.SingleArgumentEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class QueueTrackEvent extends SingleArgumentEvent {

    public static final String KEYWORD = "queue";

    public QueueTrackEvent(MessageCreateEvent event) {
        super(event);
    }
}
