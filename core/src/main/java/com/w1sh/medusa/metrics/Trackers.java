package com.w1sh.medusa.metrics;

import com.w1sh.medusa.core.events.Event;
import discord4j.core.DiscordClient;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

public final class Trackers {

    private static Instant startInstant;
    private static HashMap<Class<?>, Long> eventCount = new HashMap<>();

    public static void track(Event event){
        if(eventCount.containsKey(event.getClass())){
            Long value = eventCount.get(event.getClass()) + 1;
            eventCount.put(event.getClass(), value);
        }else eventCount.put(event.getClass(), 1L);
    }

    public static String getUptime(){
        final long seconds = Duration.between(startInstant, Instant.now()).getSeconds();
        final long absSeconds = Math.abs(seconds);
        String positive = "";
        if(absSeconds >= 3600){
            positive = String.format("%d %s, %d %s and %d %s",
                    absSeconds / 3600,
                    absSeconds / 3600 > 1 ? "hours" : "hour",
                    (absSeconds % 3600) / 60,
                    ((absSeconds % 3600) / 60) > 1 ? "minutes" : "minute",
                    absSeconds % 60,
                    absSeconds % 60 > 1 ? "seconds" : "second");
        } else if (absSeconds >= 60){
            positive = String.format("%d %s and %d %s",
                    (absSeconds % 3600) / 60,
                    ((absSeconds % 3600) / 60) > 1 ? "minutes" : "minute",
                    absSeconds % 60,
                    absSeconds % 60 > 1 ? "seconds" : "second");
        } else {
            positive = String.format("%d %s", absSeconds % 60, absSeconds % 60 > 1 ? "seconds" : "second");
        }
        return positive;
    }

    public static Long getTotalEventCount(){
        return eventCount.values().stream().reduce(Long::sum).orElse(0L);
    }

    public static Long getGuilds(DiscordClient client){
        return client.getGuilds().count().block();
    }

    public static Long getUsers(DiscordClient client){
        return client.getUsers().count().block();
    }

    public static void setStartInstant(Instant startInstant) {
        Trackers.startInstant = startInstant;
    }
}
