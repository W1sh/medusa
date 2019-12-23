package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.ClapifyEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.MultipleArgsEventListener;
import com.w1sh.medusa.utils.Messenger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ClapifyEventListener implements MultipleArgsEventListener<ClapifyEvent> {

    public ClapifyEventListener(CommandEventDispatcher eventDispatcher) {
        EventFactory.registerEvent(ClapifyEvent.KEYWORD, ClapifyEvent.class);
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<ClapifyEvent> getEventType() {
        return ClapifyEvent.class;
    }

    @Override
    public Mono<Void> execute(ClapifyEvent event) {
        return Mono.just(event)
                .filterWhen(this::validate)
                .map(ev -> ev.getMessage().getContent().orElse("").split(" "))
                .map(this::clapify)
                .doOnNext(clappedMessage -> Messenger.send(event, clappedMessage).subscribe())
                .then();
    }

    @Override
    public Mono<Boolean> validate(ClapifyEvent event) {
        return Mono.justOrEmpty(event.getMessage().getContent())
                .map(message -> message.split(" "))
                .filter(strings -> strings.length > 1)
                .hasElement();
    }

    private String clapify(String[] words){
        StringBuilder stringBuilder = new StringBuilder(":clap: ");
        for(int index = 1; index < words.length; index++){
            stringBuilder.append(words[index].toUpperCase()).append(" :clap: ");
        }
        return stringBuilder.toString();
    }

}
