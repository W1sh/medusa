package com.w1sh.medusa.listeners.playlists;

import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.events.PlaylistEvent;
import com.w1sh.medusa.listeners.CustomEventListener;
import com.w1sh.medusa.services.MessageService;
import discord4j.core.object.entity.Message;
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
        return applyAction(event).then();
    }

    private Mono<Message> applyAction(PlaylistEvent event) {
        PlaylistAction playlistAction = PlaylistAction.of(event.getArguments().get(0));
        switch (playlistAction) {
            case SAVE: return playlistSaveAction.apply(event);
            case SHOW: return playlistShowAction.apply(event);
            case DELETE: return playlistDeleteAction.apply(event);
            case LOAD: return playlistLoadAction.apply(event);
            default: return messageService.send(event.getChannel(), MessageEnum.PLAYLIST_ERROR);
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
