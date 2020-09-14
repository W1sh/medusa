package com.w1sh.medusa.actions;

import com.w1sh.medusa.data.Rule;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.events.ChannelRulesEvent;
import com.w1sh.medusa.services.ChannelService;
import com.w1sh.medusa.services.MessageService;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
public final class ChannelRulesDeactivateAction implements Function<ChannelRulesEvent, Mono<Message>> {

    private final ChannelService channelService;
    private final MessageService messageService;

    @Override
    public Mono<Message> apply(ChannelRulesEvent event) {
        final Rule rule = Rule.of(event.getArguments().get(0));

        return event.getGuildChannel()
                .flatMap(channelService::findByChannel)
                .doOnNext(channelRule -> channelRule.getRules().remove(rule))
                .flatMap(channelService::delete)
                .flatMap(ruleEnum -> messageService.send(event.getChannel(), MessageEnum.RULES_DEACTIVATED, rule.getValue()));
    }
}
