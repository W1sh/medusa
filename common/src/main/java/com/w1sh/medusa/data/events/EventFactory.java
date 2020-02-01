package com.w1sh.medusa.data.events;

import discord4j.core.event.domain.message.MessageCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

public final class EventFactory {

    private static final Logger logger = LoggerFactory.getLogger(EventFactory.class);
    private static final Map<String, Class<? extends Event>> EVENTS = new HashMap<>();
    private static final Pattern inlineEventPattern = Pattern.compile("\\{\\{.+?(?:}})");
    private static final String ARGUMENT_DELIMITER = " ";
    private static String prefix = "!";

    private EventFactory(){}

    public static Event extractEvents(final MessageCreateEvent event){
        try {
            final String message = event.getMessage().getContent().orElse("");

            if (message.startsWith(prefix)){
                final String eventKeyword = message.split(ARGUMENT_DELIMITER)[0].substring(1);
                final Class<?> clazz = EVENTS.getOrDefault(eventKeyword, UnsupportedEvent.class);
                Event e = (Event) clazz.getConstructor(MessageCreateEvent.class).newInstance(event);
                return extractArguments(e);
            } else {
                Matcher matcher = inlineEventPattern.matcher(message);
                final List<String> matches = matcher.results()
                        .map(MatchResult::group)
                        .collect(Collectors.toList());
                if(!matches.isEmpty()){
                    return extractInlineEvents(event, matches);
                }
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            logger.error("Could not access or instantiate constructor", e);
        } catch (InvocationTargetException e) {
            logger.error("Could not instantiate constructor, invocation target failed with exception", e.getTargetException());
        }
        return null;
    }

    private static Event extractInlineEvents(final MessageCreateEvent event, final List<String> matches){
        final List<InlineEvent> events = new ArrayList<>();
        try {
            int order = 1;
            for(String match : matches){
                final String argument = match.replaceAll("[{!}]", "");
                final String inlineEventPrefix = match.substring(0, 3).replaceAll("\\w", "");
                final Class<?> clazz = EVENTS.getOrDefault(inlineEventPrefix, UnsupportedEvent.class);
                InlineEvent instance = (InlineEvent) clazz.getConstructor(MessageCreateEvent.class).newInstance(event);
                instance.setInlineArgument(argument);
                instance.setInlineOrder(order++);
                events.add(instance);
            }
            if(events.size() > 1){
                final Class<?> clazz = EVENTS.getOrDefault("multiple", UnsupportedEvent.class);
                MultipleInlineEvent instance = (MultipleInlineEvent) clazz.getConstructor(MessageCreateEvent.class).newInstance(event);
                events.forEach(e -> e.setFragment(true));
                instance.setEvents(events);
                return instance;
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            logger.error("Could not access or instantiate constructor", e);
        } catch (InvocationTargetException e) {
            logger.error("Could not instantiate constructor, invocation target failed with exception", e.getTargetException());
        }
        return events.get(0);
    }

    private static Event extractArguments(final Event event){
        String[] content = event.getMessage().getContent().orElse("").split(ARGUMENT_DELIMITER);
        List<String> argumentsList = Arrays.asList(content).subList(1, content.length);
        Map<Integer, String> arguments = IntStream.range(0, argumentsList.size())
                .boxed()
                .collect(toMap(Function.identity(), argumentsList::get));
        event.setArguments(arguments);
        return event;
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
