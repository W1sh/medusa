package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.Rule;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.events.ChannelRulesEvent;
import com.w1sh.medusa.services.ChannelRuleService;
import discord4j.core.object.entity.channel.GuildChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
public final class ChannelRulesDeactivateAction implements Function<ChannelRulesEvent, Mono<? extends Response>> {

    private final ChannelRuleService channelRuleService;

    @Override
    public Mono<? extends Response> apply(ChannelRulesEvent event) {
        final Rule rule = Rule.of(event.getArguments().get(0));

        return event.getMessage().getChannel()
                .ofType(GuildChannel.class)
                .flatMap(channelRuleService::findByChannel)
                .doOnNext(channelRule -> channelRule.getRules().remove(rule))
                .flatMap(channelRuleService::delete)
                .flatMap(ruleEnum -> createRuleDeactivatedMessage(rule, event));
    }

    private Mono<TextMessage> createRuleDeactivatedMessage(Rule rule, ChannelRulesEvent event){
        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, String.format("**%s** rule has been deactivated", rule.getValue()), false));
    }
}
