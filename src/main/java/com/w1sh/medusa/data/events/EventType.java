package com.w1sh.medusa.data.events;

import discord4j.rest.util.Permission;

import java.util.Set;

import static discord4j.rest.util.Permission.*;

public enum EventType {
    MODERATION(SEND_MESSAGES,ADMINISTRATOR),
    GAMBLING(SEND_MESSAGES),
    AUDIO(SEND_MESSAGES, CONNECT),
    MTG(SEND_MESSAGES),
    OTHER(SEND_MESSAGES),
    UNKNOWN(SEND_MESSAGES);

    private final Set<Permission> permissions;

    EventType(Permission... permissions) {
        this.permissions = Set.of(permissions);
    }

    public static EventType of(String argument) {
        for (EventType value : values()) {
            if(value.name().equalsIgnoreCase(argument)) return value;
        }
        return UNKNOWN;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }
}
