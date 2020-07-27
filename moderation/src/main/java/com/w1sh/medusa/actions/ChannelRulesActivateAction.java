package com.w1sh.medusa.actions;

import com.w1sh.medusa.data.Channel;
import com.w1sh.medusa.data.Rule;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.events.ChannelRulesEvent;
import com.w1sh.medusa.services.ChannelRuleService;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.GuildChannel;
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
        final Rule rule = Rule.of(event.getArguments().get(0));
        final String guildId = event.getGuildId().map(Snowflake::asString).orElse("");

        final Mono<Channel> createChannelMono = Mono.defer(() -> event.getMessage().getChannel()
                .map(chan -> new Channel(chan.getId().asString(), guildId)));

        return event.getMessage().getChannel()
                .ofType(GuildChannel.class)
                .flatMap(channelRuleService::findByChannel)
                .switchIfEmpty(createChannelMono)
                .filter(channel -> !channel.getRules().contains(rule))
                .doOnNext(channel -> channel.getRules().add(rule))
                .flatMap(channelRuleService::save)
                .flatMap(channel -> createRuleActivatedMessage(rule, event));
    }

    private Mono<TextMessage> createRuleActivatedMessage(Rule rule, ChannelRulesEvent event){
        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, String.format("**%s** rule has been activated", rule.getValue()), false));
    }
}
