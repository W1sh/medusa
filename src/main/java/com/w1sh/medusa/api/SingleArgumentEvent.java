package com.w1sh.medusa.api;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.util.Snowflake;

public abstract class SingleArgumentEvent extends MessageCreateEvent {

    public SingleArgumentEvent(MessageCreateEvent event){
        super(event.getClient(), event.getMessage(), event.getGuildId().map(Snowflake::asLong).orElse(null),
                event.getMember().orElse(null));
    }
}
