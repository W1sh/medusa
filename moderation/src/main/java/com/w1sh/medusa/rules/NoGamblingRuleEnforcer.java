package com.w1sh.medusa.rules;

import com.w1sh.medusa.data.Rule;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.services.ChannelService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.GuildChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public final class NoGamblingRuleEnforcer implements RuleEnforcer<GuildChannel>{

    private final ChannelService channelService;

    @Override
    public Mono<Boolean> validate(GuildChannel channel) {
        return channelService.findByChannel(channel)
                .filter(cr -> cr.getRules().contains(Rule.NO_GAMBLING))
                .hasElement();
    }

    @Override
    public Mono<Response> enforce(MessageCreateEvent event) {
        Mono<Response> warningMessage = Mono.defer(() -> event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, String.format("**%s**, no gambling allowed on this channel",
                        event.getMember().map(Member::getDisplayName).orElse("")), false)));

        return Mono.fromRunnable(() -> event.getMessage().delete().subscribe().dispose())
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete message", t)))
                .then(warningMessage);
    }
}
