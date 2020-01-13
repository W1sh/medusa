package com.w1sh.medusa.metrics;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;

import java.time.Duration;
import java.time.Instant;

public final class Trackers {

    private static Instant startInstant;

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
