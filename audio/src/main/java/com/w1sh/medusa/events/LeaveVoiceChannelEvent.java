package com.w1sh.medusa.events;

import com.w1sh.medusa.core.events.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class LeaveVoiceChannelEvent extends Event {

    public static final String KEYWORD = "leave";

    public LeaveVoiceChannelEvent(MessageCreateEvent event) {
        super(event);
    }

}
