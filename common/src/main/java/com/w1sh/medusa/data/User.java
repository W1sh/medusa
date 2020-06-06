package com.w1sh.medusa.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Table(value = "core.users")
public class User {

    @Id
    private Integer id;

    @Column(value = "user_id")
    private String userId;

    public User(String userId) {
        this.userId = userId;
    }

}
