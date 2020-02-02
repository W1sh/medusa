package com.w1sh.medusa.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(value = "users")
public class User {

    @Id
    private Long id;
    private Long rolls;
    private Long rollWins;
    private Long points;

    public User(Long id, Long rolls, Long rollWins, Long points) {
        this.id = id;
        this.rolls = rolls;
        this.rollWins = rollWins;
        this.points = points;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRolls() {
        return rolls;
    }

    public void setRolls(Long rolls) {
        this.rolls = rolls;
    }

    public Long getRollWins() {
        return rollWins;
    }

    public void setRollWins(Long rollWins) {
        this.rollWins = rollWins;
    }

    public Long getPoints() {
        return points;
    }

    public void setPoints(Long points) {
        this.points = points;
    }
}
