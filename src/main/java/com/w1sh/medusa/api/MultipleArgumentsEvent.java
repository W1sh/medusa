package com.w1sh.medusa.api;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.util.Snowflake;

public abstract class MultipleArgumentsEvent extends MessageCreateEvent {

    private final Integer numAllowedArgs;

    public MultipleArgumentsEvent(MessageCreateEvent event, Integer numAllowedArgs) {
        super(event.getClient(), event.getMessage(), event.getGuildId().map(Snowflake::asLong).orElse(null),
                event.getMember().orElse(null));
        this.numAllowedArgs = numAllowedArgs;
    }

    public Integer getNumAllowedArgs() {
        return numAllowedArgs;
    }
}
