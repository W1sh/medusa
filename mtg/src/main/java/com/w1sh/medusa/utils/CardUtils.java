package com.w1sh.medusa.utils;

import com.w1sh.medusa.data.events.InlineEvent;
import com.w1sh.medusa.data.responses.Embed;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.MessageChannel;

import java.awt.*;

public class CardUtils {

    private CardUtils(){}

    public static Embed createErrorEmbed(MessageChannel messageChannel, InlineEvent event){
        return new Embed(messageChannel, embedCreateSpec -> {
            embedCreateSpec.setColor(Color.GREEN);
            embedCreateSpec.setDescription(String.format(":x: Sorry **%s**, I failed to find the card you requested, be more specific or try another card.",
                    event.getMember().flatMap(Member::getNickname).orElse("")));
        }, event.isFragment(), event.getInlineOrder());
    }
}
