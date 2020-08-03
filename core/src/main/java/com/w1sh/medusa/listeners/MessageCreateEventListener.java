package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.Rule;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.rules.NoLinksRuleEnforcer;
import com.w1sh.medusa.services.ChannelRuleService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.GuildChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class MessageCreateEventListener implements EventListener<MessageCreateEvent> {

    private final NoLinksRuleEnforcer noLinksRuleEnforcer;
    private final ResponseDispatcher responseDispatcher;
    private final ChannelRuleService channelRuleService;

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        return event.getMessage().getChannel()
                .filter(ignored -> event.getClass().equals(MessageCreateEvent.class))
                .ofType(GuildChannel.class)
                .filterWhen(channel -> channelRuleService.findByChannel(channel)
                        .filter(cr -> cr.getRules().contains(Rule.NO_LINKS))
                        .hasElement())
                .map(ignored -> event.getMessage().getContent())
                .filterWhen(noLinksRuleEnforcer::validate)
                .flatMap(ignored -> noLinksRuleEnforcer.enforce(event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }
}