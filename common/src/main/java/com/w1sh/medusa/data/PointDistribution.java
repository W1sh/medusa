package com.w1sh.medusa.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@Document
public class PointDistribution {

    @Id
    private String id;

    private Integer totalGuilds;

    private Long pointsDistributed;

    private Instant createdOn;

    public PointDistribution(Long pointsDistributed, Integer totalGuilds) {
        this.totalGuilds = totalGuilds;
        this.pointsDistributed = pointsDistributed * 100;
    }

}
