package com.w1sh.medusa.api.dice.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Registered;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Registered(prefix = "points")
public final class PointsEvent extends Event {

    public static final String KEYWORD = "points";

    public PointsEvent(MessageCreateEvent event) {
        super(event);
    }
}
