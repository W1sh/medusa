package com.w1sh.medusa.core.events;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.Snowflake;

import java.util.ArrayList;
import java.util.List;

public abstract class SingleArgumentEvent extends MessageCreateEvent {

    private final List<Permission> permissions;

    public SingleArgumentEvent(MessageCreateEvent event){
        super(event.getClient(), event.getMessage(), event.getGuildId().map(Snowflake::asLong).orElse(null),
                event.getMember().orElse(null));
        this.permissions = new ArrayList<>();
        this.permissions.add(Permission.SEND_MESSAGES);
    }

    public SingleArgumentEvent(MessageCreateEvent event, List<Permission> permissions){
        this(event);
        this.permissions.addAll(permissions);
    }

    public List<Permission> getPermissions() {
        return permissions;
    }
}
