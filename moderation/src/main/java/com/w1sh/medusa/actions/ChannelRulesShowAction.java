package com.w1sh.medusa.actions;

import com.w1sh.medusa.data.Channel;
import com.w1sh.medusa.data.Rule;
import com.w1sh.medusa.data.responses.Embed;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.events.ChannelRulesEvent;
import com.w1sh.medusa.services.ChannelRuleService;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
public final class ChannelRulesShowAction implements Function<ChannelRulesEvent, Mono<? extends Response>> {

    private final ChannelRuleService channelRuleService;

    @Override
    public Mono<? extends Response> apply(ChannelRulesEvent event) {
        final String guildId = event.getGuildId().map(Snowflake::asString).orElse("");

        final Mono<Channel> createChannelMono = Mono.defer(() -> event.getMessage().getChannel()
                .map(chan -> new Channel(chan.getId().asString(), guildId)));

        return event.getMessage().getChannel()
                .ofType(GuildChannel.class)
                .flatMap(channelRuleService::findByChannel)
                .switchIfEmpty(createChannelMono)
                .flatMap(channelRules -> channelRulesEmbed(channelRules, event));
    }

    private Mono<Embed> channelRulesEmbed(Channel channelRule, ChannelRulesEvent event) {
        return event.getMessage().getChannel()
                .map(chan -> new Embed(chan, embedCreateSpec -> {
                    embedCreateSpec.setColor(Color.GREEN);
                    embedCreateSpec.setTitle("Channel Rules");
                    embedCreateSpec.setDescription(rulesEmbedDescription(channelRule));
                }));
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
