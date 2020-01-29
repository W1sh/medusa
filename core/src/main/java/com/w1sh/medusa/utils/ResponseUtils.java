package com.w1sh.medusa.utils;

import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ResponseUtils {

    public static final String BULLET = "\u2022";
    public static final String ZERO_WIDTH_SPACE = "\u200E";

    private ResponseUtils(){}

    public static String formatDuration(Long duration){
        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }
}
