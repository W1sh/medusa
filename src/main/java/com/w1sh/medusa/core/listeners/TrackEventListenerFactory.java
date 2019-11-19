package com.w1sh.medusa.core.listeners;

import com.w1sh.medusa.core.listeners.impl.TrackEventListener;

public class TrackEventListenerFactory {

    private TrackEventListenerFactory(){}

    public static TrackEventListener build(Long guildId) {
        return new TrackEventListener(guildId);
    }
}
