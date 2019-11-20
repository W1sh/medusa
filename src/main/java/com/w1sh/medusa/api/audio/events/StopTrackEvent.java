package com.w1sh.medusa.api.audio.events;

import com.w1sh.medusa.api.CommandEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class StopTrackEvent extends CommandEvent {

    public static final String KEYWORD = "stop";

    public StopTrackEvent(MessageCreateEvent event) {
        super(event);
    }

}