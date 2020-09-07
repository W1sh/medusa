package com.w1sh.medusa.listeners.blocklist;

import com.w1sh.medusa.data.Channel;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.events.BlocklistEvent;
import com.w1sh.medusa.services.ChannelService;
import discord4j.core.object.entity.channel.GuildChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public final class BlocklistShowAction implements Function<BlocklistEvent, Mono<? extends Response>> {

    private final ChannelService channelService;

    @Override
    public Mono<? extends Response> apply(BlocklistEvent event) {
        return event.getMessage().getChannel()
                .ofType(GuildChannel.class)
                .flatMap(channelService::findByChannel)
                .filter(channel -> !channel.getBlocklist().isEmpty())
                .flatMap(channel -> blocklistedWordsMessage(channel, event))
                .switchIfEmpty(emptyListMessage(event));
    }

    private Mono<TextMessage> blocklistedWordsMessage(Channel channel, BlocklistEvent event) {
        final String blocklistedWords = String.format("Words currently blocklisted in this channel: **%s**",
                String.join(", ", channel.getBlocklist()));

        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, blocklistedWords, false));
    }

    private Mono<TextMessage> emptyListMessage(BlocklistEvent event) {
        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, "No words are blocklisted in this channel.", false));
    }
}
