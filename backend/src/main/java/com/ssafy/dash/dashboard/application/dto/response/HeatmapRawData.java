package com.ssafy.dash.dashboard.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapRawData {
    private String date;
    private Long userId;
    private String username;
    private String avatarUrl;
    private Long dailyCount;
}
