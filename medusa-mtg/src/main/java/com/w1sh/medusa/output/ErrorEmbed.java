package com.w1sh.medusa.output;

import com.w1sh.medusa.data.events.InlineEvent;
import com.w1sh.medusa.data.responses.OutputEmbed;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class ErrorEmbed extends OutputEmbed {

    private final String nickname;

    public ErrorEmbed(InlineEvent event) {
        super(event.getChannel(), event.getChannelId(), event.isFragment(), event.getInlineOrder());
        this.nickname = event.getNickname();
    }

    @Override
    protected void build() {
        this.embedCreateSpec = this.embedCreateSpec.andThen(embedCreateSpec -> {
            embedCreateSpec.setDescription(String.format(":x: Sorry **%s**, I failed to find the card you requested, be more specific or try another card.",
                    nickname));
        });
    }
}
