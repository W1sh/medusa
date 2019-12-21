package com.w1sh.medusa.events;

import com.w1sh.medusa.core.events.SingleArgumentEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.util.Permission;

import java.util.Collections;

public class JoinVoiceChannelEvent extends SingleArgumentEvent {

    public static final String KEYWORD = "join";

    public JoinVoiceChannelEvent(MessageCreateEvent event) {
        super(event, Collections.singletonList(Permission.CONNECT));
    }
}
