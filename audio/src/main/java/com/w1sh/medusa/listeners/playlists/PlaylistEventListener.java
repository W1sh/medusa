package com.w1sh.medusa.listeners.playlists;

import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.events.PlaylistEvent;
import com.w1sh.medusa.listeners.CustomEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class PlaylistEventListener implements CustomEventListener<PlaylistEvent> {

    private final PlaylistSaveAction playlistSaveAction;
    private final PlaylistShowAction playlistShowAction;
    private final PlaylistDeleteAction playlistDeleteAction;
    private final PlaylistLoadAction playlistLoadAction;
    private final MessageService messageService;

    @Override
    public Mono<Void> execute(PlaylistEvent event) {
        return applyAction(event)
                .doOnNext(messageService::queue)
                .doAfterTerminate(messageService::flush)
                .then();
    }

    private Mono<? extends Response> applyAction(PlaylistEvent event) {
        PlaylistAction playlistAction = PlaylistAction.of(event.getArguments().get(0));
        switch (playlistAction) {
            case SAVE: return playlistSaveAction.apply(event);
            case SHOW: return playlistShowAction.apply(event);
            case DELETE: return playlistDeleteAction.apply(event);
            case LOAD: return playlistLoadAction.apply(event);
            default: return event.getMessage().getChannel()
                    .map(channel -> new TextMessage(channel,
                            "Unknown playlist action, try one of the following: **SHOW**, **SAVE**, **LOAD**, **DELETE**", false));
        }
    }

    private enum PlaylistAction {
        SAVE, SHOW, DELETE, LOAD, UNKNOWN;

        public static PlaylistAction of(String string){
            for (PlaylistAction value : values()) {
                if(value.name().equalsIgnoreCase(string)) return value;
            }
            return UNKNOWN;
        }
    }

}
