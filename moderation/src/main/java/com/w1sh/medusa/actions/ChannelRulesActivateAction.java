package com.w1sh.medusa.actions;

import com.w1sh.medusa.data.Channel;
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
public final class ChannelRulesActivateAction implements Function<ChannelRulesEvent, Mono<Message>> {

    private final ChannelService channelService;
    private final MessageService messageService;

    @Override
    public Mono<Message> apply(ChannelRulesEvent event) {
        final Rule rule = Rule.of(event.getArguments().get(0));

        final Mono<Channel> createChannelMono = Mono.defer(() -> event.getMessage().getChannel()
                .map(chan -> new Channel(chan.getId().asString(), event.getGuildId())));

        return event.getGuildChannel()
                .flatMap(channelService::findByChannel)
                .switchIfEmpty(createChannelMono)
                .filter(channel -> !channel.getRules().contains(rule))
                .doOnNext(channel -> channel.getRules().add(rule))
                .flatMap(channelService::save)
                .flatMap(channel -> messageService.send(event.getChannel(), MessageEnum.RULES_ACTIVATED, rule.getValue()));
    }
}
