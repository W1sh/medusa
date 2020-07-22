package com.w1sh.medusa.data;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(value = "core.rules")
public class Rule {

    @Id
    private Integer id;

    private RuleEnum ruleValue;
}
