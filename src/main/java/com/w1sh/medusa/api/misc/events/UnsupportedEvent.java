package com.w1sh.medusa.api.misc.events;

import com.w1sh.medusa.core.events.SingleArgumentEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class UnsupportedEvent extends SingleArgumentEvent {

    public UnsupportedEvent(MessageCreateEvent event) {
        super(event);
    }

}
