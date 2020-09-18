package com.w1sh.medusa.output;

import com.w1sh.medusa.data.events.InlineEvent;
import com.w1sh.medusa.data.responses.OutputEmbed;
import com.w1sh.medusa.resources.Card;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
public class ArtworkEmbed extends OutputEmbed {

    private final Card card;

    public ArtworkEmbed(@NonNull Card card, InlineEvent event) {
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
}
