package com.w1sh.medusa.core.events;

import com.w1sh.medusa.api.misc.events.UnsupportedEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class EventFactory {

    private static final Logger logger = LoggerFactory.getLogger(EventFactory.class);
    private static final Map<String, Class<? extends MessageCreateEvent>> EVENTS = new HashMap<>();
    private static String prefix = "!";

    private EventFactory(){}

    public static Optional<MessageCreateEvent> createEvent(MessageCreateEvent event){
        try {
            String message = event.getMessage().getContent().orElse("");
            Class<?> clazz;
            if (message.startsWith(prefix)){
                String eventKeyword = message.split(" ")[0].substring(1);
                clazz = EVENTS.getOrDefault(eventKeyword, UnsupportedEvent.class);
            } else if (hasInlineEvent(message)){
                String inlineEventPrefix = message.substring(message.indexOf("{{")).substring(0, 3).replaceAll("\\w", "");
                clazz = EVENTS.getOrDefault(inlineEventPrefix, UnsupportedEvent.class);
            } else {
                return Optional.empty();
            }
            Object instance = clazz.getConstructor(MessageCreateEvent.class).newInstance(event);
            return Optional.of((MessageCreateEvent) instance);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            logger.error("Could not access or instantiate constructor", e);
        } catch (InvocationTargetException e) {
            logger.error("Could not instantiate constructor, invocation target failed with exception", e.getTargetException());
        }
        return Optional.empty();
    }

    public static void registerEvent(String keyword, Class<? extends MessageCreateEvent> clazz){
        EVENTS.put(keyword, clazz);
    }

    private static boolean hasInlineEvent(String message){
        String inlineEventRegex = "\\{\\{.+?(?:}})";
        return Pattern.compile(inlineEventRegex).matcher(message).find();
    }

    public static String getPrefix() {
        return prefix;
    }

    public static void setPrefix(String prefix) {
        EventFactory.prefix = prefix;
    }
}
