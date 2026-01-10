package com.ssafy.dash.decoration.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Decoration {
    private Long id;
    private String name;
    private String description;
    private String cssClass;
    private String type; // PUBLIC, ADMIN_ONLY
    private Integer price;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static Decoration create(String name, String description, String cssClass, String type, Integer price,
            Boolean isActive) {
        Decoration d = new Decoration();
        d.name = name;
        d.description = description;
        d.cssClass = cssClass;
        d.type = type;
        d.price = price != null ? price : 0;
        d.isActive = isActive != null ? isActive : true;
        d.createdAt = LocalDateTime.now();
        return d;
    }
}
