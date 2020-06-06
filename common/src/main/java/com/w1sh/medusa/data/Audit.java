package com.w1sh.medusa.data;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;

@Data
public class Audit {

    @Column(value = "created_on")
    private LocalDateTime createdOn;

    @Column(value = "updated_on")
    private LocalDateTime updatedOn;

    public LocalDateTime getCreatedOn() {
        return createdOn != null ? createdOn : LocalDateTime.now();
    }

}
