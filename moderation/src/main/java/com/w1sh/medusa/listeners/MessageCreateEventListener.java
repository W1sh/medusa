package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.RuleEnum;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.rules.NoLinksRule;
import com.w1sh.medusa.services.ChannelRuleService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class MessageCreateEventListener implements EventListener<MessageCreateEvent> {

    private final NoLinksRule noLinksRule;
    private final ResponseDispatcher responseDispatcher;
    private final ChannelRuleService channelRuleService;

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        return event.getMessage().getChannel()
                .filter(ignored -> event.getClass().equals(MessageCreateEvent.class))
                .flatMap(messageChannel -> channelRuleService.findByChannelAndRuleEnum(messageChannel.getId().asString(), RuleEnum.NO_LINKS))
                .flatMap(channelRule -> noLinksRule.validate(event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }
}
