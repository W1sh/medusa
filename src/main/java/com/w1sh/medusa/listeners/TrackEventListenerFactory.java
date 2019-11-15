package com.w1sh.medusa.listeners;

import com.w1sh.medusa.listeners.impl.TrackEventListener;
import org.springframework.stereotype.Component;

@Component
public class TrackEventListenerFactory {

    private TrackEventListenerFactory(){}

    public static TrackEventListener build(Long guildId) {
        return new TrackEventListener(guildId);
    }
}
