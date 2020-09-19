package com.w1sh.medusa.output;

import com.w1sh.medusa.data.events.InlineEvent;
import com.w1sh.medusa.data.responses.OutputEmbed;
import com.w1sh.medusa.resources.Card;
import discord4j.core.object.reaction.ReactionEmoji;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
public class SearchEmbed extends OutputEmbed {

    private final List<Card> cards;

    public SearchEmbed(@NonNull List<Card> cards, InlineEvent event) {
        super(event.getChannel(), event.getChannelId(), event.isFragment(), event.getInlineOrder());
        this.cards = cards;
    }

    @Override
    protected void build() {
        this.embedCreateSpec = this.embedCreateSpec.andThen(embedCreateSpec -> {
            for (int i = 0; i < Math.min(cards.size(), 5); i++) {
                embedCreateSpec.addField(String.format("%d - %s - %s", (i+1), cards.get(i).getName(), cards.get(i).getTypeLine()),
                        cards.get(i).getOracleText(), false);
            }
            if(cards.size() > 5) {
                this.getReactions().add(ReactionEmoji.unicode("\u2B05"));
                this.getReactions().add(ReactionEmoji.unicode("\u27A1"));
            }
        });
    }
}
