package com.w1sh.medusa.data.events;

import com.w1sh.medusa.data.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Type(prefix = "multiple")
@EqualsAndHashCode(callSuper = true)
public final class MultipleInlineEvent extends Event {

    @Getter @Setter
    private List<InlineEvent> events;

    public MultipleInlineEvent(MessageCreateEvent event) {
        super(event);
    }

}
