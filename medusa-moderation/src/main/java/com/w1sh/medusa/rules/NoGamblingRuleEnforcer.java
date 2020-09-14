package com.w1sh.medusa.rules;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.Rule;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.services.ChannelService;
import com.w1sh.medusa.services.MessageService;
import discord4j.core.object.entity.Message;
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
    private final MessageService messageService;

    public Mono<Boolean> validate(GuildChannel channel) {
        return channelService.findByChannel(channel)
                .filter(cr -> cr.getRules().contains(Rule.NO_GAMBLING))
                .hasElement();
    }

    public Mono<Message> enforce(Event event) {
        Mono<Message> warningMessage = Mono.defer(() -> messageService.send(event.getChannel(), MessageEnum.NOGAMBLING, event.getNickname()));

        return Mono.fromRunnable(() -> event.getMessage().delete().subscribe().dispose())
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete message", t)))
                .then(warningMessage);
    }
}
