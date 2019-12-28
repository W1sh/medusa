package com.w1sh.medusa.core.events;

import com.w1sh.medusa.api.misc.events.UnsupportedEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EventFactory {

    private static final Logger logger = LoggerFactory.getLogger(EventFactory.class);
    private static final Map<String, Class<? extends Event>> EVENTS = new HashMap<>();
    private static final Pattern inlineEventPattern = Pattern.compile("\\{\\{.+?(?:}})");
    private static String prefix = "!";

    private EventFactory(){}

    public static List<Event> extractEvents(final MessageCreateEvent event){
        final List<Event> events = new ArrayList<>();
        try {
            final String message = event.getMessage().getContent().orElse("");

            if (message.startsWith(prefix)){
                final String eventKeyword = message.split(" ")[0].substring(1);
                final Class<?> clazz = EVENTS.getOrDefault(eventKeyword, UnsupportedEvent.class);
                final Event instance = (Event) clazz.getConstructor(MessageCreateEvent.class).newInstance(event);
                events.add(instance);
            } else {
                Matcher matcher = inlineEventPattern.matcher(message);
                final List<String> matches = matcher.results()
                        .map(MatchResult::group)
                        .collect(Collectors.toList());
                for(String match : matches){
                    final String argument = match.replaceAll("[{}]", "");
                    final String inlineEventPrefix = match.substring(0, 3).replaceAll("\\w", "");
                    final Class<?> clazz = EVENTS.getOrDefault(inlineEventPrefix, UnsupportedEvent.class);
                    Event instance = (Event) clazz.getConstructor(MessageCreateEvent.class).newInstance(event);
                    instance.setInlineArgument(argument);
                    events.add(instance);
                }
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            logger.error("Could not access or instantiate constructor", e);
        } catch (InvocationTargetException e) {
            logger.error("Could not instantiate constructor, invocation target failed with exception", e.getTargetException());
        }
        return events;
    }

    public static void registerEvent(final String keyword, final Class<? extends Event> clazz){
        EVENTS.put(keyword, clazz);
    }

    public static String getPrefix() {
        return prefix;
    }

    public static void setPrefix(String prefix) {
        EventFactory.prefix = prefix;
    }
}
