package com.w1sh.medusa.api;

import com.w1sh.medusa.api.audio.events.*;
import com.w1sh.medusa.api.dice.events.DuelRollEvent;
import com.w1sh.medusa.api.dice.events.RollEvent;
import com.w1sh.medusa.api.misc.events.PingEvent;
import com.w1sh.medusa.api.misc.events.UnsupportedEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CommandEventFactory {

    private static final Logger logger = LoggerFactory.getLogger(CommandEventFactory.class);
    private static final Map<String, Class<? extends MessageCreateEvent>> EVENTS = new HashMap<>();
    public static final String PREFIX = "!";

    static {
        EVENTS.put(PingEvent.KEYWORD, PingEvent.class);
        EVENTS.put(JoinVoiceChannelEvent.KEYWORD, JoinVoiceChannelEvent.class);
        EVENTS.put(LeaveVoiceChannelEvent.KEYWORD, LeaveVoiceChannelEvent.class);
        EVENTS.put(NextTrackEvent.KEYWORD, NextTrackEvent.class);
        EVENTS.put(PlayTrackEvent.KEYWORD, PlayTrackEvent.class);
        EVENTS.put(PauseTrackEvent.KEYWORD, PauseTrackEvent.class);
        EVENTS.put(QueueTrackEvent.KEYWORD, QueueTrackEvent.class);
        EVENTS.put(ResumeTrackEvent.KEYWORD, ResumeTrackEvent.class);
        EVENTS.put(RollEvent.KEYWORD, RollEvent.class);
        EVENTS.put(DuelRollEvent.KEYWORD, DuelRollEvent.class);
    }

    private CommandEventFactory(){}

    public static Optional<MessageCreateEvent> createEvent(MessageCreateEvent event){
        try {
            Class<?> clazz = EVENTS.getOrDefault(event.getMessage().getContent()
                    .map(String::toLowerCase)
                    .map(msg -> msg.split(" ")[0].substring(1))
                    .orElse(""), UnsupportedEvent.class);
            Object instance = clazz.getConstructor(MessageCreateEvent.class).newInstance(event);
            return Optional.of((MessageCreateEvent) instance);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            logger.error("Could not access or instantiate constructor", e);
        } catch (InvocationTargetException e) {
            logger.error("Could not instantiate constructor, invocation target failed with exception", e.getTargetException());
        }
        return Optional.empty();
    }

}
