package com.w1sh.medusa.api.audio.events;

import com.w1sh.medusa.api.SingleArgumentEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class QueueTrackEvent extends SingleArgumentEvent {

    public static final String KEYWORD = "queue";

    public QueueTrackEvent(MessageCreateEvent event) {
        super(event);
    }
}
