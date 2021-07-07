package com.w1sh.medusa.listeners;

import com.w1sh.medusa.core.Instance;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.events.UptimeEvent;
import com.w1sh.medusa.services.MessageService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class UptimeEventListener implements CustomEventListener<UptimeEvent> {

    private final MessageService messageService;

    public UptimeEventListener(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public Mono<Void> execute(UptimeEvent event) {
        return messageService.send(event.getChannel(), MessageEnum.UPTIME_SUCCESS, Instance.getUptime())
                .then();
    }
}
