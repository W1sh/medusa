package com.w1sh.medusa.rules;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.Rule;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.services.ChannelService;
import discord4j.core.object.entity.channel.GuildChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public final class NoGamblingRuleEnforcer {

    private final ChannelService channelService;

    public Mono<Boolean> validate(GuildChannel channel) {
        return channelService.findByChannel(channel)
                .filter(cr -> cr.getRules().contains(Rule.NO_GAMBLING))
                .hasElement();
    }

    public Mono<Response> enforce(Event event) {
        Mono<Response> warningMessage = Mono.defer(() -> event.getChannel()
                .map(chan -> new TextMessage(chan, String.format("**%s**, no gambling allowed on this channel",
                        event.getNickname()), false)));

        return Mono.fromRunnable(() -> event.getMessage().delete().subscribe().dispose())
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete message", t)))
                .then(warningMessage);
    }
}
