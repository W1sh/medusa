package com.w1sh.medusa.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Registered;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Registered(prefix = "resume")
public final class ResumeTrackEvent extends Event {

    public static final String KEYWORD = "resume";

    public ResumeTrackEvent(MessageCreateEvent event) {
        super(event);
    }
}
