package com.w1sh.medusa.listeners.blocklist;

import com.w1sh.medusa.data.Channel;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.events.BlocklistEvent;
import com.w1sh.medusa.services.ChannelService;
import com.w1sh.medusa.services.MessageService;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public final class BlocklistShowAction implements Function<BlocklistEvent, Mono<Message>> {

    private final ChannelService channelService;
    private final MessageService messageService;

    @Override
    public Mono<Message> apply(BlocklistEvent event) {
        return event.getGuildChannel()
                .flatMap(channelService::findByChannel)
                .filter(channel -> !channel.getBlocklist().isEmpty())
                .flatMap(channel -> blocklistedWordsMessage(channel, event))
                .switchIfEmpty(messageService.send(event.getChannel(), MessageEnum.BLOCKLIST_SHOW_ERROR));
    }

    private Mono<Message> blocklistedWordsMessage(Channel channel, BlocklistEvent event) {
        final String blocklistedWords = String.format("Words currently blocklisted in this channel: **%s**",
                String.join(", ", channel.getBlocklist()));

        return messageService.send(event.getChannel(), blocklistedWords);
    }
}
