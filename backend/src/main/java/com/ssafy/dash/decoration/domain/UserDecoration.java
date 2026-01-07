package com.ssafy.dash.decoration.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UserDecoration {
    private Long id;
    private Long userId;
    private Long decorationId;
    private LocalDateTime acquiredAt;
    
    // Join field
    private Decoration decoration;

    public static UserDecoration create(Long userId, Long decorationId) {
        UserDecoration ud = new UserDecoration();
        ud.userId = userId;
        ud.decorationId = decorationId;
        ud.acquiredAt = LocalDateTime.now();
        return ud;
    }
}
