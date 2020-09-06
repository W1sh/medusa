package com.w1sh.medusa.listeners.blocklist;

import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.events.BlocklistEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public final class BlocklistRemoveAction implements Function<BlocklistEvent, Mono<? extends Response>> {

    @Override
    public Mono<? extends Response> apply(BlocklistEvent playlistEvent) {
        return Mono.empty();
    }
}
