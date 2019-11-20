package com.w1sh.medusa.api.audio.events;

import com.w1sh.medusa.api.SingleArgumentEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class JoinVoiceChannelEvent extends SingleArgumentEvent {

    public static final String KEYWORD = "join";

    public JoinVoiceChannelEvent(MessageCreateEvent event) {
        super(event);
    }

}
