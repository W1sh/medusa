package com.w1sh.medusa.output;

import com.w1sh.medusa.data.events.InlineEvent;
import com.w1sh.medusa.data.responses.OutputEmbed;
import com.w1sh.medusa.rest.resources.Card;

import java.util.Objects;

public class ArtworkEmbed extends OutputEmbed {

    private final Card card;

    public ArtworkEmbed(Card card, InlineEvent event) {
        super(event.getChannel(), event.getChannelId(), event.isFragment(), event.getInlineOrder());
        this.card = card;
    }

    @Override
    protected void build() {
        this.embedCreateSpec = this.embedCreateSpec.andThen(embedCreateSpec -> {
            embedCreateSpec.setImage(card.getImage().getArtwork());
            embedCreateSpec.setFooter("Artwork by: " + card.getArtist(), null);
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ArtworkEmbed that = (ArtworkEmbed) o;
        return Objects.equals(card, that.card);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), card);
    }
}
