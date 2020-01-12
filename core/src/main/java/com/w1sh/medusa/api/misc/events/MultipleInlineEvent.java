package com.w1sh.medusa.api.misc.events;

import com.w1sh.medusa.core.events.Event;
import com.w1sh.medusa.core.events.InlineEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

import java.util.List;

public class MultipleInlineEvent extends Event {

    public static final String KEYWORD = "multiple";
    private List<InlineEvent> events;

    public MultipleInlineEvent(MessageCreateEvent event) {
        super(event);
    }

    public List<InlineEvent> getEvents() {
        return events;
    }

    public void setEvents(List<InlineEvent> events) {
        this.events = events;
    }

}
