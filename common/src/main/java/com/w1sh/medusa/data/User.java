package com.w1sh.medusa.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(value = "core.users")
public class User {

    @Id
    private Long id;
    private Long userId;
    private Long rolls;
    private Long duelrolls;
    private Long points;

    public User(Long userId) {
        this.userId = userId;
        this.rolls = 0L;
        this.duelrolls = 0L;
        this.points = 0L;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRolls() {
        return rolls;
    }

    public void setRolls(Long rolls) {
        this.rolls = rolls;
    }

    public Long getDuelrolls() {
        return duelrolls;
    }

    public void setDuelrolls(Long duelrolls) {
        this.duelrolls = duelrolls;
    }

    public Long getPoints() {
        return points;
    }

    public void setPoints(Long points) {
        this.points = points;
    }
}
