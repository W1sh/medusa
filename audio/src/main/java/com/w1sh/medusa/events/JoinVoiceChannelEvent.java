package com.w1sh.medusa.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Permission;

import java.util.Collections;

@Type(prefix = "join")
public final class JoinVoiceChannelEvent extends Event {

    public JoinVoiceChannelEvent(MessageCreateEvent event) {
        super(event, Collections.singletonList(Permission.CONNECT));
    }
}
