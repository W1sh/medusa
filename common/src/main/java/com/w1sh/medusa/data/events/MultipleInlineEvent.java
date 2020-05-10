package com.w1sh.medusa.data.events;

import discord4j.core.event.domain.message.MessageCreateEvent;

import java.util.List;

@Registered(prefix = "multiple")
public final class MultipleInlineEvent extends Event {

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
