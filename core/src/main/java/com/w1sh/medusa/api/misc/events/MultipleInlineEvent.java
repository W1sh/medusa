package com.w1sh.medusa.api.misc.events;

import com.w1sh.medusa.core.events.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

import java.util.List;

public class MultipleInlineEvent extends Event {

    public static final String KEYWORD = "multiple";
    private List<Event> events;

    public MultipleInlineEvent(MessageCreateEvent event) {
        super(event);
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

}
