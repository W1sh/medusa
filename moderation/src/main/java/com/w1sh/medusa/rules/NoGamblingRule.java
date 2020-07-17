package com.w1sh.medusa.rules;

import com.w1sh.medusa.data.RuleEnum;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.services.ChannelRuleService;
import com.w1sh.medusa.utils.Reactive;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class NoGamblingRule {

    private final ChannelRuleService channelRuleService;

    public Mono<Boolean> isNoGamblingActive(MessageCreateEvent event){
        return event.getMessage().getChannel()
                .filterWhen(messageChannel -> channelRuleService.hasRule(messageChannel.getId().asString(), RuleEnum.NO_GAMBLING))
                .hasElement()
                .transform(Reactive.ifElse(bool -> deleteMessage(event), bool -> Mono.just(false)));
    }

    public Mono<TextMessage> createNoGamblingMessage(MessageCreateEvent event){
        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, String.format("**%s**, no gambling allowed on this channel",
                        event.getMember().map(Member::getDisplayName).orElse("")), false));
    }

    private Mono<Boolean> deleteMessage(MessageCreateEvent event){
        return Mono.fromRunnable(() -> event.getMessage().delete().subscribe().dispose())
                .then(Mono.just(true));
    }
}
