package com.w1sh.medusa.api.audio.events;

import com.w1sh.medusa.api.CommandEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class PlayTrackEvent extends CommandEvent {

    public PlayTrackEvent(MessageCreateEvent event) {
        super(event);
    }

}
