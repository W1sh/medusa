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
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public final class BlocklistAddAction implements Function<BlocklistEvent, Mono<Message>> {

    private final ChannelService channelService;
    private final MessageService messageService;

    @Override
    public Mono<Message> apply(BlocklistEvent event) {
        if (StringUtils.isEmpty(event.getArguments().get(1))) return messageService.send(event.getChannel(), MessageEnum.BLOCKLIST_ADD_ERROR);
        final String blockedWord = event.getArguments().get(1);

        final Mono<Channel> createChannelMono = Mono.defer(() -> event.getChannel()
                .map(chan -> new Channel(chan.getId().asString(), event.getGuildId())));

        return event.getGuildChannel()
                .flatMap(channelService::findByChannel)
                .switchIfEmpty(createChannelMono)
                .doOnNext(channel -> channel.getBlocklist().add(blockedWord))
                .flatMap(channelService::save)
                .flatMap(ignored -> messageService.send(event.getChannel(), MessageEnum.BLOCKLIST_ADD_SUCCESS));
    }
}