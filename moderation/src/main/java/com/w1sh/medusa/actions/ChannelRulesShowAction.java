package com.w1sh.medusa.actions;

import com.w1sh.medusa.data.Channel;
import com.w1sh.medusa.data.Rule;
import com.w1sh.medusa.events.ChannelRulesEvent;
import com.w1sh.medusa.services.ChannelService;
import com.w1sh.medusa.services.MessageService;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public final class ChannelRulesShowAction implements Function<ChannelRulesEvent, Mono<Message>> {

    private final ChannelService channelService;
    private final MessageService messageService;

    @Override
    public Mono<Message> apply(ChannelRulesEvent event) {
        final Mono<Channel> createChannelMono = Mono.defer(() -> event.getMessage().getChannel()
                .map(chan -> new Channel(chan.getId().asString(), event.getGuildId())));

        return event.getGuildChannel()
                .flatMap(channelService::findByChannel)
                .switchIfEmpty(createChannelMono)
                .flatMap(channelRules -> channelRulesEmbed(channelRules, event));
    }

    private Mono<Message> channelRulesEmbed(Channel channelRule, ChannelRulesEvent event) {
        final Consumer<EmbedCreateSpec> embedCreateSpecConsumer = embedCreateSpec -> {
            embedCreateSpec.setColor(Color.GREEN);
            embedCreateSpec.setTitle("Channel Rules");
            embedCreateSpec.setDescription(rulesEmbedDescription(channelRule));
        };
        return messageService.send(event.getChannel(), embedCreateSpecConsumer);
    }

    private String rulesEmbedDescription(Channel channelRule){
        final StringBuilder stringBuilder = new StringBuilder();
        for(Rule rule : Rule.values()){
            stringBuilder.append(String.format("**%s** rule is `%s`%n", rule.getValue(), isRuleActive(channelRule, rule) ? "on" : "off"));
        }
        return stringBuilder.toString();
    }

    private boolean isRuleActive(Channel channelRule, Rule rule){
        return channelRule.getRules().contains(rule);
    }
}
