package com.w1sh.medusa.data.events;

import discord4j.core.event.domain.message.MessageCreateEvent;

import discord4j.rest.util.Permission;
import discord4j.rest.util.Snowflake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Event extends MessageCreateEvent {

    private Integer minArguments;
    private Map<Integer, String> arguments;
    private List<Permission> permissions;

    public Event(MessageCreateEvent event){
        super(event.getClient(), event.getShardInfo(), event.getMessage(),
                event.getGuildId().map(Snowflake::asLong).orElseThrow(),
                event.getMember().orElseThrow());
        this.minArguments = 0;
        this.arguments = new HashMap<>();
        this.permissions = new ArrayList<>();
        this.permissions.add(Permission.SEND_MESSAGES);
    }

    public Event(MessageCreateEvent event, Integer minArguments) {
        this(event);
        this.minArguments = minArguments;
    }

    public Event(MessageCreateEvent event, List<Permission> permissions){
        this(event);
        this.permissions.addAll(permissions);
    }

    public Integer getMinArguments() {
        return minArguments;
    }

    public void setMinArguments(Integer minArguments) {
        this.minArguments = minArguments;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    public Map<Integer, String> getArguments() {
        return arguments;
    }

    public void setArguments(Map<Integer, String> arguments) {
        this.arguments = arguments;
    }
}
