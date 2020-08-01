package com.w1sh.medusa.data.events;

public enum EventType {
    MODERATION, GAMBLING, AUDIO, MTG, OTHER, UNKNOWN;

    public static EventType of(String argument) {
        for (EventType value : values()) {
            if(value.name().equalsIgnoreCase(argument)) return value;
        }
        return UNKNOWN;
    }
}
