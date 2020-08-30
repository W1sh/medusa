package com.w1sh.medusa.api.misc.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Permission;

import java.util.List;

@Type(prefix = "purge")
public final class PurgeEvent extends Event {

    public PurgeEvent(MessageCreateEvent event) {
        super(event, List.of(Permission.ADMINISTRATOR));
    }
}
