package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.ChannelRule;
import com.w1sh.medusa.data.RuleEnum;
import com.w1sh.medusa.events.ChannelRulesEvent;
import com.w1sh.medusa.services.ChannelRuleService;
import com.w1sh.medusa.data.responses.Embed;
import com.w1sh.medusa.data.responses.Response;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public final class ChannelRulesShowAction implements Function<ChannelRulesEvent, Mono<? extends Response>> {

    private final ChannelRuleService channelRuleService;

    @Override
    public Mono<? extends Response> apply(ChannelRulesEvent event) {
        return event.getMessage().getChannel()
                .flatMap(messageChannel -> channelRuleService.findAllByChannel(messageChannel.getId().asString()))
                .flatMap(channelRules -> channelRulesEmbed(channelRules, event));
    }

    private Mono<Embed> channelRulesEmbed(List<ChannelRule> channelRuleList, ChannelRulesEvent event) {
        return event.getMessage().getChannel()
                .map(chan -> new Embed(chan, embedCreateSpec -> {
                    embedCreateSpec.setColor(Color.GREEN);
                    embedCreateSpec.setTitle("Channel Rules");
                    embedCreateSpec.setDescription(rulesEmbedDescription(channelRuleList));
                }));
    }

    private String rulesEmbedDescription(List<ChannelRule> channelRules){
        StringBuilder stringBuilder = new StringBuilder();
        for(RuleEnum ruleEnum : RuleEnum.values()){
            stringBuilder.append(String.format("**%s** rule is `%s`%n", ruleEnum.getValue(), isRuleActive(channelRules, ruleEnum) ? "on" : "off"));
        }
        return stringBuilder.toString();
    }

    private boolean isRuleActive(List<ChannelRule> channelRules, RuleEnum ruleEnum){
        return channelRules.stream().anyMatch(channelRule -> channelRule.getRule().getRuleValue().equals(ruleEnum));
    }
}
