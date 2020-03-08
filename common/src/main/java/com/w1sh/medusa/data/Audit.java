package com.w1sh.medusa.data;

import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;

public class Audit {

    @Column(value = "created_on")
    private LocalDateTime createdOn;

    @Column(value = "updated_on")
    private LocalDateTime updatedOn;

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }
}
