package com.w1sh.medusa.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Table(value = "core.point_distribution")
public class PointDistribution {

    @Id
    private Integer id;

    @Column(value = "total_guilds")
    private Integer totalGuilds;

    @Column(value = "points_distributed")
    private Long pointsDistributed;

    @Column(value = "created_on")
    private LocalDateTime createdOn;

    public PointDistribution(Long pointsDistributed, Integer totalGuilds) {
        this.totalGuilds = totalGuilds;
        this.pointsDistributed = pointsDistributed * 100;
    }

}
