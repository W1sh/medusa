package com.w1sh.medusa.listeners.blocklist;

import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.events.BlocklistEvent;
import com.w1sh.medusa.listeners.CustomEventListener;
import com.w1sh.medusa.services.MessageService;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class BlocklistEventListener implements CustomEventListener<BlocklistEvent> {

    private final BlocklistAddAction blocklistAddAction;
    private final BlocklistRemoveAction blocklistRemoveAction;
    private final BlocklistShowAction blocklistShowAction;
    private final MessageService messageService;

    @Override
    public Mono<Void> execute(BlocklistEvent event) {
        return applyAction(event).then();
    }

    private Mono<Message> applyAction(BlocklistEvent event) {
        if(event.getArguments().isEmpty()) return blocklistShowAction.apply(event);

        BlocklistAction blocklistAction = BlocklistAction.of(event.getArguments().get(0));
        switch (blocklistAction) {
            case ADD: return blocklistAddAction.apply(event);
            case REMOVE: return blocklistRemoveAction.apply(event);
            case SHOW: return blocklistShowAction.apply(event);
            default: return messageService.send(event.getChannel(), MessageEnum.BLOCKLIST_ERROR);
        }
    }

    private enum BlocklistAction {
        ADD, REMOVE, SHOW, UNKNOWN;

        public static BlocklistAction of(String string){
            for (BlocklistAction value : values()) {
                if(value.name().equalsIgnoreCase(string)) return value;
            }
            return UNKNOWN;
        }
    }
}
