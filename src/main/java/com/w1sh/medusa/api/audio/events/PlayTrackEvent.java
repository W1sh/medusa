package com.w1sh.medusa.api.audio.events;

import com.w1sh.medusa.api.MultipleArgumentsEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.util.Permission;

import java.util.Collections;

public class PlayTrackEvent extends MultipleArgumentsEvent {

    public static final String KEYWORD = "play";
    private static final Integer NUM_ALLOWED_ARGS = 2;

    public PlayTrackEvent(MessageCreateEvent event) {
        super(event, Collections.singletonList(Permission.MANAGE_MESSAGES), NUM_ALLOWED_ARGS);
    }

}
