package com.w1sh.medusa.core.events;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.Snowflake;

import java.util.ArrayList;
import java.util.List;

public abstract class MultipleArgumentsEvent extends MessageCreateEvent {

    private final List<Permission> permissions;
    private final Integer numAllowedArgs;

    public MultipleArgumentsEvent(MessageCreateEvent event, Integer numAllowedArgs) {
        super(event.getClient(), event.getMessage(), event.getGuildId().map(Snowflake::asLong).orElse(null),
                event.getMember().orElse(null));
        this.permissions = new ArrayList<>();
        this.permissions.add(Permission.SEND_MESSAGES);
        this.numAllowedArgs = numAllowedArgs;
    }

    public MultipleArgumentsEvent(MessageCreateEvent event, List<Permission> permissions, Integer numAllowedArgs) {
        this(event, numAllowedArgs);
        this.permissions.addAll(permissions);
    }

    public Integer getNumAllowedArgs() {
        return numAllowedArgs;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }
}
