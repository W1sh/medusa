package com.w1sh.medusa.data.events;

import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.Getter;
import lombok.Setter;

public abstract class InlineEvent extends Event {

    @Getter @Setter
    private boolean fragment;

    @Getter @Setter
    private String inlineArgument;

    @Getter @Setter
    private Integer inlineOrder;

    public InlineEvent(MessageCreateEvent event) {
        super(event);
    }
}
