package com.w1sh.medusa.data.events;

import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

@Component
public final class EventFactory {

    private static final Logger logger = LoggerFactory.getLogger(EventFactory.class);

    private static final Pattern INLINE_EVENT_PATTERN = Pattern.compile("\\{\\{.+?(?:}})");
    private static final Pattern INLINE_SPECIALS_PATTERN = Pattern.compile("[{!?}]");
    private static final Pattern WORD_PATTERN = Pattern.compile("\\w");
    private static final String ARGUMENT_DELIMITER = " ";

    private final Map<String, Class<? extends Event>> events;

    @Getter @Setter
    private String prefix;

    public EventFactory() {
        this.events = new HashMap<>(10);
        this.prefix = "!";
    }

    public Mono<Event> extractEvents(final MessageCreateEvent event){
        final String message = event.getMessage().getContent();

        if (message.startsWith(prefix)){
            final String eventKeyword = message.split(ARGUMENT_DELIMITER)[0].substring(1);
            final Event e = createInstance(eventKeyword, event);
            if(e != null){
                return Mono.justOrEmpty(extractArguments(e));
            } else return Mono.empty();
        } else {
            final List<String> matches = INLINE_EVENT_PATTERN.matcher(message).results()
                    .map(MatchResult::group)
                    .collect(Collectors.toList());
            return Mono.justOrEmpty(extractInlineEvents(event, matches));
        }
    }

    private Event extractInlineEvents(final MessageCreateEvent event, final List<String> matches){
        final List<InlineEvent> inlineEvents = new ArrayList<>();

        int order = 1;
        for(String match : matches){
            final String argument = INLINE_SPECIALS_PATTERN.matcher(match).replaceAll("");
            final String inlineEventPrefix = WORD_PATTERN.matcher(match.substring(0, 3)).replaceAll("");
            final InlineEvent inlineEvent = (InlineEvent) createInstance(inlineEventPrefix, event);
            if (inlineEvent != null) {
                inlineEvent.setInlineArgument(argument);
                inlineEvent.setInlineOrder(order++);
                inlineEvents.add(inlineEvent);
            }
        }
        if (inlineEvents.size() == 1) {
            return inlineEvents.get(0);
        }
        if (inlineEvents.size() > 1) {
            final MultipleInlineEvent multipleInlineEvent = (MultipleInlineEvent) createInstance("multiple", event);
            if (multipleInlineEvent != null) {
                inlineEvents.forEach(e -> e.setFragment(true));
                multipleInlineEvent.setEvents(inlineEvents);
                return multipleInlineEvent;
            }
        }
        return null;
    }

    private Event extractArguments(final Event event){
        final String[] content = event.getMessage().getContent().split(ARGUMENT_DELIMITER);
        final List<String> argumentsList = Arrays.asList(content).subList(1, content.length);
        Map<Integer, String> arguments = IntStream.range(0, argumentsList.size())
                .boxed()
                .collect(toMap(Function.identity(), argumentsList::get));
        event.setArguments(arguments);
        return event;
    }

    private Event createInstance(final String prefix, final MessageCreateEvent event) {
        try {
            final Class<? extends Event> clazz = events.getOrDefault(prefix, UnsupportedEvent.class);
            return clazz.getConstructor(MessageCreateEvent.class).newInstance(event);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            logger.error("Could not access or instantiate constructor", e);
        } catch (InvocationTargetException e) {
            logger.error("Could not instantiate constructor, invocation target failed with exception", e.getTargetException());
        }
        return null;
    }

    public void registerEvent(final String keyword, final Class<? extends Event> clazz){
        if(events.containsKey(keyword)){
            logger.error("Failed to register event! Event with keyword <{}> is already registered!", keyword);
            return;
        }
        events.put(keyword, clazz);
    }
}
