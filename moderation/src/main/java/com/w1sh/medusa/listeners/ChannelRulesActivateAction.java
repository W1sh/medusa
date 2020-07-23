package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.ChannelRule;
import com.w1sh.medusa.data.Rule;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.events.ChannelRulesEvent;
import com.w1sh.medusa.services.ChannelRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
public final class ChannelRulesActivateAction implements Function<ChannelRulesEvent, Mono<? extends Response>> {

    private final ChannelRuleService channelRuleService;

    @Override
    public Mono<? extends Response> apply(ChannelRulesEvent event) {
        return Mono.justOrEmpty(Rule.of(event.getArguments().get(0)))
                .zipWith(event.getMessage().getChannel(), (rule, messageChannel) -> new ChannelRule(messageChannel.getId().asString(), rule))
                .flatMap(channelRuleService::save)
                .flatMap(channelRule -> createRuleActivatedMessage(channelRule, event));
    }

    private Mono<TextMessage> createRuleActivatedMessage(ChannelRule channelRule, ChannelRulesEvent event){
        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, String.format("**%s** rule has been activated", channelRule.getRule().getValue()), false));
    }
}
