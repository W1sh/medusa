package com.w1sh.medusa.data.events;

import discord4j.rest.util.Permission;

import java.util.Set;

import static discord4j.rest.util.Permission.SEND_MESSAGES;

public enum EventType {
    MTG(SEND_MESSAGES),
    OTHER(SEND_MESSAGES);

    private final Set<Permission> permissions;

    EventType(Permission... permissions) {
        this.permissions = Set.of(permissions);
    }

    public static EventType of(String argument) {
        for (EventType value : values()) {
            if(value.name().equalsIgnoreCase(argument)) return value;
        }
        return OTHER;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }
}
