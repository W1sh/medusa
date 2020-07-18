package com.w1sh.medusa.data;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table(value = "core.warnings")
public class Warning {

    @Id
    private Integer id;

    @Column(value = "fk_user")
    private User user;

    @Column(value = "created_on")
    private LocalDateTime createdOn;
}
