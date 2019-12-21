package com.w1sh.medusa;

import com.w1sh.medusa.listeners.TrackEventListener;

public class TrackEventListenerFactory {

    private TrackEventListenerFactory(){}

    public static TrackEventListener build(Long guildId) {
        return new TrackEventListener(guildId);
    }
}
