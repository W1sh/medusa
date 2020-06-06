package com.w1sh.medusa.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Registered;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Registered(prefix = "loop")
public class LoopEvent extends Event {

    public LoopEvent(MessageCreateEvent event) {
        super(event);
    }

}
