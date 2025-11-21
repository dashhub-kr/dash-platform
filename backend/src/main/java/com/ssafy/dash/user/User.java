package com.ssafy.dash.user;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;

    private Long githubId;

    private String username;

    private String avatarUrl;

    private String solvedAcHandle;

    private String accessToken;

    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

}
