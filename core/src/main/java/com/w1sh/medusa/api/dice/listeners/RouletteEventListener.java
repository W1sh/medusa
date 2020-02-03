package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.events.RouletteEvent;
import com.w1sh.medusa.data.events.EventFactory;
import com.w1sh.medusa.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.service.UserService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RouletteEventListener implements EventListener<RouletteEvent> {

    private final ResponseDispatcher responseDispatcher;
    private final UserService userService;

    public RouletteEventListener(ResponseDispatcher responseDispatcher, UserService userService, CommandEventDispatcher eventDispatcher) {
        this.responseDispatcher = responseDispatcher;
        this.userService = userService;
        EventFactory.registerEvent(RouletteEvent.KEYWORD, RouletteEvent.class);
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<RouletteEvent> getEventType() {
        return RouletteEvent.class;
    }

    @Override
    public Mono<Void> execute(RouletteEvent event) {
        return null;
    }
}
