package com.w1sh.medusa.api.audio.events;

import com.w1sh.medusa.api.CommandEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class JoinVoiceChannelEvent extends CommandEvent {

    public JoinVoiceChannelEvent(MessageCreateEvent event) {
        super(event);
    }

}
