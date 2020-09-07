package com.w1sh.medusa.listeners.blocklist;

import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.events.BlocklistEvent;
import com.w1sh.medusa.services.ChannelService;
import discord4j.core.object.entity.channel.GuildChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public final class BlocklistRemoveAction implements Function<BlocklistEvent, Mono<? extends Response>> {

    private final ChannelService channelService;

    @Override
    public Mono<? extends Response> apply(BlocklistEvent event) {
        if (StringUtils.isEmpty(event.getArguments().get(1))) return errorMessage(event);
        final String blockedWord = event.getArguments().get(1);

        return event.getMessage().getChannel()
                .ofType(GuildChannel.class)
                .flatMap(channelService::findByChannel)
                .doOnNext(channel -> channel.getBlocklist().remove(blockedWord))
                .flatMap(channelService::save)
                .flatMap(ignored -> createRuleActivatedMessage(event));
    }

    private Mono<TextMessage> createRuleActivatedMessage(BlocklistEvent event){
        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, "Word has been removed from the blocklist", false));
    }

    private Mono<? extends Response> errorMessage(BlocklistEvent event){
        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, "No word received. Nothing to remove from the blocklist", false));
    }
}
