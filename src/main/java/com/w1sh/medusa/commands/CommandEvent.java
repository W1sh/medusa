package com.w1sh.medusa.commands;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.Event;

public abstract class CommandEvent extends Event {

    protected CommandEvent(DiscordClient client) {
        super(client);
    }
}
