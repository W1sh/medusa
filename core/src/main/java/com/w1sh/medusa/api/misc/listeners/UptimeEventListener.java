package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.UptimeEvent;
import com.w1sh.medusa.core.Instance;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.listeners.CustomEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class UptimeEventListener implements CustomEventListener<UptimeEvent> {

    private final MessageService messageService;

    @Override
    public Mono<Void> execute(UptimeEvent event) {
        return event.getChannel()
                .map(chan -> new TextMessage(chan,
                        String.format("Medusa has been online for %s", Instance.getUptime()), false))
                .doOnNext(messageService::queue)
                .doAfterTerminate(messageService::flush)
                .then();
    }
}
