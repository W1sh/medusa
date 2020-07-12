package com.w1sh.medusa.api.moderation.converters;

import com.w1sh.medusa.api.moderation.data.ChannelRule;
import com.w1sh.medusa.api.moderation.data.Rule;
import io.r2dbc.spi.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.data.r2dbc.mapping.SettableValue;
import org.springframework.stereotype.Component;

public final class ChannelRuleConverter {

    private ChannelRuleConverter(){}

    @Component
    @ReadingConverter
    public static class ChannelRuleReadConverter implements Converter<Row, ChannelRule> {

        public ChannelRule convert(Row source) {
            ChannelRule channelRule = new ChannelRule();
            channelRule.setId(source.get("id", Integer.class));
            channelRule.setChannel(source.get("channel_id", String.class));

            Rule rule = new Rule();
            rule.setId(source.get("rule_id", Integer.class));

            channelRule.setRule(rule);
            return channelRule;
        }
    }

    @Component
    @WritingConverter
    public static class ChannelRuleWriteConverter implements Converter<ChannelRule, OutboundRow> {

        @Override
        public OutboundRow convert(ChannelRule source) {
            OutboundRow row = new OutboundRow();
            row.put("id", SettableValue.fromOrEmpty(source.getId(), Integer.class));
            row.put("channel_id", SettableValue.from(source.getChannel()));
            row.put("rule_id", SettableValue.from(source.getRule().getId()));
            return row;
        }
    }
}
