package com.w1sh.medusa.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(value = "core.point_distribution")
public class PointDistribution {

    @Id
    private Integer id;

    @Column(value = "total_guilds")
    private Integer totalGuilds;

    @Column(value = "points_distributed")
    private Long pointsDistributed;

    @Column(value = "time_elapsed")
    private Long timeElapsed;

    @Column(value = "created_on")
    private LocalDateTime createdOn;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTotalGuilds() {
        return totalGuilds;
    }

    public void setTotalGuilds(Integer totalGuilds) {
        this.totalGuilds = totalGuilds;
    }

    public Long getPointsDistributed() {
        return pointsDistributed;
    }

    public void setPointsDistributed(Long pointsDistributed) {
        this.pointsDistributed = pointsDistributed;
    }

    public Long getTimeElapsed() {
        return timeElapsed;
    }

    public void setTimeElapsed(Long timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }
}
