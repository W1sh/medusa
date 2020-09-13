package com.w1sh.medusa.data.events;

import com.w1sh.medusa.data.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class InlineEvent extends Event {

    private boolean fragment;

    private String inlineArgument;

    private Integer inlineOrder;

    public InlineEvent(MessageCreateEvent event) {
        super(event);
    }

    public boolean hasArgument(){
        return inlineArgument != null && !inlineArgument.isBlank();
    }
}
