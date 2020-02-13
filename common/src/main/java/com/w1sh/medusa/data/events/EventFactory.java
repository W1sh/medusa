package com.w1sh.medusa.data.events;

import discord4j.core.event.domain.message.MessageCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

@Component
public final class EventFactory {

    private static final Logger logger = LoggerFactory.getLogger(EventFactory.class);

    private static final Pattern INLINE_EVENT_PATTERN = Pattern.compile("\\{\\{.+?(?:}})");
    private static final Pattern INLINE_SPECIALS_PATTERN = Pattern.compile("[{!}]");
    private static final Pattern WORD_PATTERN = Pattern.compile("\\w");
    private static final String ARGUMENT_DELIMITER = " ";

    private String prefix;
    private final Map<String, Class<? extends Event>> events;

    public EventFactory() {
        this.events = new HashMap<>(10);
        this.prefix = "!";
    }

    public Event extractEvents(final MessageCreateEvent event){
        try {
            final String message = event.getMessage().getContent().orElse("");

            if (message.startsWith(prefix)){
                final String eventKeyword = message.split(ARGUMENT_DELIMITER)[0].substring(1);
                final Class<?> clazz = events.getOrDefault(eventKeyword, UnsupportedEvent.class);
                Event e = (Event) clazz.getConstructor(MessageCreateEvent.class).newInstance(event);
                return extractArguments(e);
            } else {
                final Matcher matcher = INLINE_EVENT_PATTERN.matcher(message);
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

    private Event extractInlineEvents(final MessageCreateEvent event, final List<String> matches){
        final List<InlineEvent> inlineEvents = new ArrayList<>();
        try {
            int order = 1;
            for(String match : matches){
                final String argument = INLINE_SPECIALS_PATTERN.matcher(match).replaceAll("");
                final String inlineEventPrefix = WORD_PATTERN.matcher(match.substring(0, 3)).replaceAll("");
                final Class<?> clazz = events.getOrDefault(inlineEventPrefix, UnsupportedEvent.class);
                final InlineEvent inlineEvent = (InlineEvent) clazz.getConstructor(MessageCreateEvent.class).newInstance(event);
                inlineEvent.setInlineArgument(argument);
                inlineEvent.setInlineOrder(order++);
                inlineEvents.add(inlineEvent);
            }
            if(inlineEvents.size() > 1){
                final Class<?> clazz = events.getOrDefault("multiple", UnsupportedEvent.class);
                final MultipleInlineEvent multipleInlineEvent = (MultipleInlineEvent) clazz.getConstructor(MessageCreateEvent.class).newInstance(event);
                inlineEvents.forEach(e -> e.setFragment(true));
                multipleInlineEvent.setEvents(inlineEvents);
                return multipleInlineEvent;
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            logger.error("Could not access or instantiate constructor", e);
        } catch (InvocationTargetException e) {
            logger.error("Could not instantiate constructor, invocation target failed with exception", e.getTargetException());
        }
        return inlineEvents.get(0);
    }

    private Event extractArguments(final Event event){
        final String[] content = event.getMessage().getContent().orElse("").split(ARGUMENT_DELIMITER);
        final List<String> argumentsList = Arrays.asList(content).subList(1, content.length);
        Map<Integer, String> arguments = IntStream.range(0, argumentsList.size())
                .boxed()
                .collect(toMap(Function.identity(), argumentsList::get));
        event.setArguments(arguments);
        return event;
    }

    public void registerEvent(final String keyword, final Class<? extends Event> clazz){
        events.put(keyword, clazz);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
