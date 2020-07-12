package com.w1sh.medusa.api.moderation.listeners;

import com.w1sh.medusa.api.moderation.data.RuleEnum;
import com.w1sh.medusa.api.moderation.events.ChannelRulesEvent;
import com.w1sh.medusa.api.moderation.services.ChannelRuleService;
import com.w1sh.medusa.api.moderation.services.RuleService;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.utils.Reactive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class ChannelRulesDeactivateAction implements Function<ChannelRulesEvent, Mono<? extends Response>> {

    private final ChannelRuleService channelRuleService;
    private final RuleService ruleService;

    @Override
    public Mono<? extends Response> apply(ChannelRulesEvent event) {
        return Mono.justOrEmpty(event.getArguments().get(0))
                .map(RuleEnum::of)
                .flatMap(ruleService::findByRuleEnum)
                .transform(Reactive.flatZipWith(event.getMessage().getChannel(),
                        (rule, messageChannel) -> channelRuleService.findByChannelAndRule(messageChannel.getId().asString(), rule)))
                .flatMap(channelRuleService::delete)
                .map(ignored -> RuleEnum.of(event.getArguments().get(0)))
                .flatMap(ruleEnum -> createRuleDeactivatedMessage(ruleEnum, event));
    }

    private Mono<TextMessage> createRuleDeactivatedMessage(RuleEnum ruleEnum, ChannelRulesEvent event){
        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, String.format("**%s** rule has been deactivated", ruleEnum.getValue()), false));
    }
}
