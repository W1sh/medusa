package com.w1sh.medusa.data;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Table(value = "core.tracks")
public final class Track {

    @Id
    private Integer id;

    private String author;

    private String title;

    private String uri;

    private Long duration;

}
