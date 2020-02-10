package com.w1sh.medusa.events;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Registered;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.util.Permission;

import java.util.Collections;

@Registered(prefix = "join")
public final class JoinVoiceChannelEvent extends Event {

    public static final String KEYWORD = "join";

    public JoinVoiceChannelEvent(MessageCreateEvent event) {
        super(event, Collections.singletonList(Permission.CONNECT));
    }
}
