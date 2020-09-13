package com.w1sh.medusa.events;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;

@Type(prefix = "leave")
public final class LeaveVoiceChannelEvent extends Event {

    public LeaveVoiceChannelEvent(MessageCreateEvent event) {
        super(event);
    }

}
