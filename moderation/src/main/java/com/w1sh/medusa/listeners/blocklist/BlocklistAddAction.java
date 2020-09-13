package com.w1sh.medusa.listeners.blocklist;

import com.w1sh.medusa.data.Channel;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.events.BlocklistEvent;
import com.w1sh.medusa.services.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public final class BlocklistAddAction implements Function<BlocklistEvent, Mono<? extends Response>> {

    private final ChannelService channelService;

    @Override
    public Mono<? extends Response> apply(BlocklistEvent event) {
        if (StringUtils.isEmpty(event.getArguments().get(1))) return errorMessage(event);
        final String blockedWord = event.getArguments().get(1);

        final Mono<Channel> createChannelMono = Mono.defer(() -> event.getChannel()
                .map(chan -> new Channel(chan.getId().asString(), event.getGuildId())));

        return event.getGuildChannel()
                .flatMap(channelService::findByChannel)
                .switchIfEmpty(createChannelMono)
                .doOnNext(channel -> channel.getBlocklist().add(blockedWord))
                .flatMap(channelService::save)
                .flatMap(ignored -> blocklistedMessage(event));
    }

    private Mono<? extends Response> blocklistedMessage(BlocklistEvent event){
        return event.getChannel().map(chan -> new TextMessage(chan, "Word has been added to the blocklist", false));
    }

    private Mono<? extends Response> errorMessage(BlocklistEvent event){
        return event.getChannel().map(chan -> new TextMessage(chan, "No word received. Nothing to add to the blocklist", false));
    }
}
