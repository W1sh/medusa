package com.w1sh.medusa.events;

import com.w1sh.medusa.core.events.MultipleArgumentsEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class PlayTrackEvent extends MultipleArgumentsEvent {

    public static final String KEYWORD = "play";
    private static final Integer NUM_ALLOWED_ARGS = 2;

    public PlayTrackEvent(MessageCreateEvent event) {
        super(event, NUM_ALLOWED_ARGS);
    }

}
