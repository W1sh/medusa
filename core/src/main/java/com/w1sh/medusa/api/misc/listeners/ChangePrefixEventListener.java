package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.ChangePrefixEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.MultipleArgsEventListener;
import com.w1sh.medusa.utils.Messenger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ChangePrefixEventListener implements MultipleArgsEventListener<ChangePrefixEvent> {

    public ChangePrefixEventListener(CommandEventDispatcher eventDispatcher) {
        EventFactory.registerEvent(ChangePrefixEvent.KEYWORD, ChangePrefixEvent.class);
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<ChangePrefixEvent> getEventType() {
        return ChangePrefixEvent.class;
    }

    @Override
    public Mono<Void> execute(ChangePrefixEvent event) {
        return Mono.just(event)
                .filterWhen(this::validate)
                .map(ev -> ev.getMessage().getContent().orElse("").split(" ")[1])
                .doOnNext(prefix -> {
                    EventFactory.setPrefix(prefix);
                    Messenger.send(event, String.format("Change prefix to \"%s\"", prefix)).subscribe();
                })
                .then();
    }

    @Override
    public Mono<Boolean> validate(ChangePrefixEvent event) {
        return Mono.justOrEmpty(true);
    }
}
