package com.w1sh.medusa.core;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.events.InlineEvent;
import com.w1sh.medusa.data.events.MultipleInlineEvent;
import com.w1sh.medusa.data.events.Type;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public final class EventFactory {

    private static final Pattern INLINE_EVENT_PATTERN = Pattern.compile("\\{\\{.+?(?:}})");
    private static final Pattern INLINE_SPECIALS_PATTERN = Pattern.compile("[{!?}]");
    private static final Pattern WORD_PATTERN = Pattern.compile("\\w");
    private static final String ARGUMENT_DELIMITER = " ";

    private final Map<String, Class<? extends Event>> events = new ConcurrentHashMap<>(0);

    @Getter @Setter
    private String prefix;

    public EventFactory() {
        this.prefix = "!";
    }

    public Event extractEvents(final MessageCreateEvent event){
        final var content = event.getMessage().getContent();
        final var type = extract(content);
        switch (type) {
            case EVENT:
                final String eventKeyword = content.split(ARGUMENT_DELIMITER)[0].substring(1);
                final Event e = createInstance(eventKeyword, event);
                return e != null ? extractArguments(e) : null;
            case INLINE_EVENT:
                final var matches = INLINE_EVENT_PATTERN.matcher(content).results()
                        .map(MatchResult::group)
                        .collect(Collectors.toList());
                return extractInlineEvents(event, matches);
            default: return null;
        }
    }

    public void registerEvent(final Class<? extends Event> clazz){
        Type type = clazz.getAnnotation(Type.class);
        if (type == null) {
            log.error("Failed to register event! The event has no Type annotation present");
            return;
        }
        if (events.containsKey(type.prefix())){
            log.error("Failed to register event! Event with keyword <{}> is already registered!", type.prefix());
            return;
        }
        events.put(type.prefix(), clazz);
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
        final var content = event.getMessage().getContent().split(ARGUMENT_DELIMITER);
        event.setArguments(Arrays.asList(content).subList(1, content.length));
        return event;
    }

    private Event createInstance(final String prefix, final MessageCreateEvent event) {
        try {
            final Class<? extends Event> clazz = events.get(prefix);
            if(clazz == null) return null;
            return clazz.getConstructor(MessageCreateEvent.class).newInstance(event);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            log.error("Could not create an instance of the event", e);
        }
        return null;
    }

    private MessageType extract(final String content) {
        if (content.startsWith(prefix)) return MessageType.EVENT;
        if (INLINE_EVENT_PATTERN.matcher(content).find()) return MessageType.INLINE_EVENT;
        return MessageType.MESSAGE;
    }

    private enum MessageType {
        MESSAGE, EVENT, INLINE_EVENT
    }
}
