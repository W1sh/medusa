package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.RuleEnum;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.rules.NoLinksRule;
import com.w1sh.medusa.services.ChannelRuleService;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class MessageUpdateEventListener implements EventListener<MessageUpdateEvent> {

    private final NoLinksRule noLinksRule;
    private final ChannelRuleService channelRuleService;
    private final ResponseDispatcher responseDispatcher;

    @Override
    public Class<MessageUpdateEvent> getEventType() {
        return MessageUpdateEvent.class;
    }

    @Override
    public Mono<Void> execute(MessageUpdateEvent event) {
        return Mono.justOrEmpty(event)
                .filter(MessageUpdateEvent::isContentChanged)
                .flatMap(MessageUpdateEvent::getChannel)
                .flatMap(messageChannel -> channelRuleService.findByChannelAndRuleEnum(messageChannel.getId().asString(), RuleEnum.NO_LINKS))
                .flatMap(channelRule -> noLinksRule.validate(event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }
}
