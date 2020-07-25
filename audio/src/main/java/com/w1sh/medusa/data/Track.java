package com.w1sh.medusa.data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public final class Track {

    private String author;

    private String title;

    private String uri;

    private Long duration;

}
