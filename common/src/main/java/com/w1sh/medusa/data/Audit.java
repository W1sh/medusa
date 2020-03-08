package com.w1sh.medusa.data;

import java.time.LocalDateTime;

public class Audit {

    private LocalDateTime createdOn;

    private LocalDateTime updateOn;

    public Audit() {
        createdOn = LocalDateTime.now();
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getUpdateOn() {
        return updateOn;
    }

    public void setUpdateOn(LocalDateTime updateOn) {
        this.updateOn = updateOn;
    }
}
