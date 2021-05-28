package com.w1sh.medusa.output;

import com.w1sh.medusa.data.events.InlineEvent;
import com.w1sh.medusa.data.responses.OutputEmbed;
import com.w1sh.medusa.resources.Card;
import discord4j.core.object.reaction.ReactionEmoji;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
public class SearchEmbed extends OutputEmbed {

    private final List<Card> cards;
    private final int maxPages;
    private final int page;

    public SearchEmbed(@NonNull List<Card> cards, InlineEvent event) {
        super(event.getChannel(), event.getChannelId(), event.isFragment(), event.getInlineOrder());
        this.cards = cards;
        this.maxPages = (int) Math.ceil(cards.size() / (double) 5);
        this.page = 0;
    }

    private SearchEmbed(SearchEmbed other, int page) {
        super(other.getMessageChannelMono(), other.getChannelId(), other.isFragment(), other.getOrder());
        this.cards = other.cards;
        this.maxPages = other.maxPages;
        this.page = page;
    }

    public static SearchEmbed nextOf(SearchEmbed embed) {
        if ((embed.page + 1) == embed.maxPages) throw new IllegalArgumentException("Impossible to create. Page can not be higher than maximum pages.");
        return new SearchEmbed(embed, embed.page + 1);
    }

    public static SearchEmbed previousOf(SearchEmbed embed) {
        if (embed.page == 0) throw new IllegalArgumentException("Impossible to create. Page can not be lower than 0");
        return new SearchEmbed(embed, embed.page - 1);
    }

    @Override
    protected void build() {
        this.embedCreateSpec = this.embedCreateSpec.andThen(embedCreateSpec -> {
            for (int i = (page*5); i < Math.min(cards.size(), (page+1)*5); i++) {
                embedCreateSpec.addField(String.format("%d - %s - %s", (i+1), cards.get(i).getName(), cards.get(i).getTypeLine()),
                        cards.get(i).getOracleText(), false);
            }
            embedCreateSpec.setFooter(String.format("%s results found - Page %s of %s",
                    cards.size(), (page+1), maxPages), null);
            this.getReactions().add(ReactionEmoji.unicode("\u2B05"));
            this.getReactions().add(ReactionEmoji.unicode("\u27A1"));
        });
    }
}
