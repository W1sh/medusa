package com.w1sh.medusa.validators;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.Rule;
import com.w1sh.medusa.data.Warning;
import com.w1sh.medusa.data.events.EventType;
import com.w1sh.medusa.data.events.Type;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.services.ChannelService;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.services.WarningService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

import static com.w1sh.medusa.utils.Reactive.isEmpty;

@Slf4j
@Component
public final class GamblingRuleValidator implements Validator<MessageCreateEvent> {

    private final ChannelService channelService;
    private final MessageService messageService;
    private final WarningService warningService;
    private final Set<String> gamblingEventsPrefixes;

    public GamblingRuleValidator(ChannelService channelService, MessageService messageService,
                                 WarningService warningService, Reflections reflections) {
        this.channelService = channelService;
        this.messageService = messageService;
        this.warningService = warningService;
        this.gamblingEventsPrefixes = reflections.getSubTypesOf(Event.class).stream()
                .filter(event -> event.getAnnotation(Type.class) != null && event.getAnnotation(Type.class).eventType().equals(EventType.GAMBLING))
                .map(event -> event.getAnnotation(Type.class).prefix())
                .collect(Collectors.toSet());
    }

    @Override
    public Mono<Boolean> validate(MessageCreateEvent event) {
        return event.getMessage().getChannel()
                .flatMap(chan -> channelService.containsRule(chan.getId().asString(), Rule.NO_GAMBLING))
                .map(ignored -> event.getMessage().getContent())
                .filterWhen(this::containsGamblingEvent)
                .flatMap(ignored -> enforce(event))
                .transform(isEmpty());
    }

    public Mono<Message> enforce(MessageCreateEvent event) {
        final Mono<Message> warningMessage = messageService.send(event.getMessage().getChannel(),
                MessageEnum.NOGAMBLING, event.getMember().map(Member::getDisplayName).orElse(""));

        return warningService.addWarning(new Warning(event))
                .flatMap(ignored -> event.getMessage().delete())
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete message", t)))
                .then(warningMessage);
    }

    private Mono<Boolean> containsGamblingEvent(String content) {
        final boolean hasGamblingEvent = gamblingEventsPrefixes.contains(content.split(" ")[0].substring(1));
        return Mono.justOrEmpty(hasGamblingEvent);
    }
}
