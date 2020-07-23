package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.Rule;
import com.w1sh.medusa.rules.NoLinksRuleEnforcer;
import com.w1sh.medusa.services.ChannelRuleService;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class MessageUpdateEventListener implements EventListener<MessageUpdateEvent> {

    private final NoLinksRuleEnforcer noLinksRuleEnforcer;
    private final ChannelRuleService channelRuleService;

    @Override
    public Class<MessageUpdateEvent> getEventType() {
        return MessageUpdateEvent.class;
    }

    @Override
    public Mono<Void> execute(MessageUpdateEvent event) {
        return Mono.justOrEmpty(event)
                .filter(MessageUpdateEvent::isContentChanged)
                .flatMap(MessageUpdateEvent::getChannel)
                .flatMap(messageChannel -> channelRuleService.findByChannelAndRule(messageChannel.getId().asString(), Rule.NO_LINKS))
                .filterWhen(channelRule -> event.getMessage()
                        .map(Message::getContent)
                        .flatMap(noLinksRuleEnforcer::validate))
                .doOnNext(ignored -> event.getMessage().flatMap(Message::delete).subscribe())
                .then();
    }
}
