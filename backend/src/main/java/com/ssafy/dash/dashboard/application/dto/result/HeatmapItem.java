package com.ssafy.dash.dashboard.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapItem {
    private String date;
    private Long count;
    private List<String> contributors;

    public static HeatmapItem of(String date, Long count, List<String> contributors) {
        return new HeatmapItem(date, count, contributors);
    }
}
