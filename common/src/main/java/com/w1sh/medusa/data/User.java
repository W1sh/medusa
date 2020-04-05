package com.w1sh.medusa.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

@Table(value = "core.users")
public class User {

    @Id
    private Long id;

    @Column(value = "user_id")
    private Long userId;

    private Long rolls;

    @Column(value = "duel_rolls")
    private Long duelRolls;

    private Long points;

    @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY)
    private Audit audit;

    public User() { }

    public User(Long userId) {
        this.userId = userId;
        this.rolls = 0L;
        this.duelRolls = 0L;
        this.points = 0L;
        this.audit = new Audit();
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

    public Long getDuelRolls() {
        return duelRolls;
    }

    public void setDuelRolls(Long duelRolls) {
        this.duelRolls = duelRolls;
    }

    public Long getPoints() {
        return points;
    }

    public void setPoints(Long points) {
        this.points = points;
    }

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }
}
