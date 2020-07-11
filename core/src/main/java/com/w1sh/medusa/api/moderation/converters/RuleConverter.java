package com.w1sh.medusa.api.moderation.converters;

import com.w1sh.medusa.api.moderation.data.Rule;
import com.w1sh.medusa.api.moderation.data.RuleEnum;
import io.r2dbc.spi.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.data.r2dbc.mapping.SettableValue;
import org.springframework.stereotype.Component;

public final class RuleConverter {

    private RuleConverter(){}

    @Component
    @ReadingConverter
    public static class RuleReadConverter implements Converter<Row, Rule> {

        public Rule convert(Row source) {
            Rule rule = new Rule();
            rule.setId(source.get("id", Integer.class));
            rule.setRuleValue(RuleEnum.of(source.get("role", String.class)));
            return rule;
        }
    }

    @Component
    @WritingConverter
    public static class RuleWriteConverter implements Converter<Rule, OutboundRow> {

        @Override
        public OutboundRow convert(Rule source) {
            OutboundRow row = new OutboundRow();
            row.put("id", SettableValue.fromOrEmpty(source.getId(), Integer.class));
            row.put("role", SettableValue.from(source.getRuleValue()));
            return row;
        }
    }
}
