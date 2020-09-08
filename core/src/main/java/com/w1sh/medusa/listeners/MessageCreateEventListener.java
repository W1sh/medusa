package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.rules.BlocklistRuleEnforcer;
import com.w1sh.medusa.rules.NoLinksRuleEnforcer;
import com.w1sh.medusa.services.ChannelService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.GuildChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class MessageCreateEventListener implements EventListener<MessageCreateEvent> {

    private final NoLinksRuleEnforcer noLinksRuleEnforcer;
    private final BlocklistRuleEnforcer blocklistRuleEnforcer;
    private final ResponseDispatcher responseDispatcher;
    private final ChannelService channelService;

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        final Mono<Response> noLinksRuleMono = Mono.defer(() -> Mono.justOrEmpty(event.getMessage().getContent())
                .filterWhen(noLinksRuleEnforcer::validate)
                .flatMap(ignored -> noLinksRuleEnforcer.enforce(event)));

        final Mono<Response> blocklistRuleMono = Mono.defer(() -> Mono.justOrEmpty(event)
                .filterWhen(blocklistRuleEnforcer::validate)
                .flatMap(ignored -> blocklistRuleEnforcer.enforce(event)));

        return event.getMessage().getChannel()
                .filter(ignored -> event.getClass().equals(MessageCreateEvent.class))
                .ofType(GuildChannel.class)
                .flatMap(channelService::findByChannel)
                .then(noLinksRuleMono)
                .switchIfEmpty(blocklistRuleMono)
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }
}
