package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.UptimeEvent;
import com.w1sh.medusa.core.Instance;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.listeners.CustomEventListener;
import com.w1sh.medusa.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class UptimeEventListener implements CustomEventListener<UptimeEvent> {

    private final MessageService messageService;

    @Override
    public Mono<Void> execute(UptimeEvent event) {
        return messageService.send(event.getChannel(), MessageEnum.UPTIME_SUCCESS, Instance.getUptime())
                .then();
    }
}
