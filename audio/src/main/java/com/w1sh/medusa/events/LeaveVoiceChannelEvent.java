package com.w1sh.medusa.events;

import com.w1sh.medusa.core.events.SingleArgumentEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class LeaveVoiceChannelEvent extends SingleArgumentEvent {

    public static final String KEYWORD = "leave";

    public LeaveVoiceChannelEvent(MessageCreateEvent event) {
        super(event);
    }

}
