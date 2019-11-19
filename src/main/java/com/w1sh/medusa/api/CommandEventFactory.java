package com.w1sh.medusa.api;

import com.w1sh.medusa.api.audio.events.JoinVoiceChannelEvent;
import com.w1sh.medusa.api.audio.events.LeaveVoiceChannelEvent;
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
    private static final Map<String, Class<? extends CommandEvent>> EVENTS = new HashMap<>();

    static {
        EVENTS.put("ping", PingEvent.class);
        EVENTS.put("join", JoinVoiceChannelEvent.class);
        EVENTS.put("leave", LeaveVoiceChannelEvent.class);
    }

    private CommandEventFactory(){}

    public static Optional<CommandEvent> createEvent(MessageCreateEvent event){
        try {
            Class<?> clazz = EVENTS.getOrDefault(event.getMessage().getContent()
                            .map(msg -> msg.substring(1))
                            .orElse(""), UnsupportedEvent.class);
            Object instance = clazz.getConstructor(MessageCreateEvent.class).newInstance(event);
            return Optional.of((CommandEvent) instance);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            logger.error("Could not access or instantiate constructor", e);
        } catch (InvocationTargetException e) {
            logger.error("Could not instantiate constructor, invocation target failed with exception", e.getTargetException());
        }
        return Optional.empty();
    }

}
