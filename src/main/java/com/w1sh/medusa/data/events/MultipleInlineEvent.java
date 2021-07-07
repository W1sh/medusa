package com.w1sh.medusa.data.events;

import com.w1sh.medusa.data.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

import java.util.List;
import java.util.Objects;

@Type(prefix = "multiple")
public final class MultipleInlineEvent extends Event {

    private List<InlineEvent> events;

    public MultipleInlineEvent(MessageCreateEvent event) {
        super(event);
    }

    public List<InlineEvent> getEvents() {
        return this.events;
    }

    public void setEvents(List<InlineEvent> events) {
        this.events = events;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MultipleInlineEvent that = (MultipleInlineEvent) o;
        return Objects.equals(events, that.events);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), events);
    }
}
