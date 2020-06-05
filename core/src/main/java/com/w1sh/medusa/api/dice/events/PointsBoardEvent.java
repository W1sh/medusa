package com.w1sh.medusa.api.dice.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Registered;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Registered(prefix = "pointsboard")
public final class PointsBoardEvent extends Event {

    public PointsBoardEvent(MessageCreateEvent event) {
        super(event);
    }
}
