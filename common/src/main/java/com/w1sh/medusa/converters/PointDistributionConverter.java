package com.w1sh.medusa.converters;

import com.w1sh.medusa.data.PointDistribution;
import io.r2dbc.spi.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.data.r2dbc.mapping.SettableValue;

import java.time.LocalDateTime;

public class PointDistributionConverter {

    private PointDistributionConverter(){}

    @ReadingConverter
    public static class PointDistributionReadConverter implements Converter<Row, PointDistribution> {

        @Override
        public PointDistribution convert(Row source) {
            PointDistribution pointDistribution = new PointDistribution();
            pointDistribution.setId(source.get("id", Integer.class));
            pointDistribution.setTotalGuilds(source.get("total_guilds", Integer.class));
            pointDistribution.setPointsDistributed(source.get("points_distributed", Long.class));
            pointDistribution.setTimeElapsed(source.get("time_elapsed", Long.class));
            pointDistribution.setCreatedOn(source.get("created_on", LocalDateTime.class));
            return pointDistribution;
        }
    }

    @WritingConverter
    public static class PointDistributionWriteConverter implements Converter<PointDistribution, OutboundRow> {

        public OutboundRow convert(PointDistribution source) {
            OutboundRow row = new OutboundRow();
            row.put("id", SettableValue.fromOrEmpty(source.getId(), Integer.class));
            row.put("total_guilds", SettableValue.from(source.getTotalGuilds()));
            row.put("points_distributed", SettableValue.from(source.getPointsDistributed()));
            row.put("time_elapsed", SettableValue.from(source.getTimeElapsed()));
            row.put("created_on", SettableValue.from(LocalDateTime.now()));
            return row;
        }
    }
}
