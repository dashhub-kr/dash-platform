package com.ssafy.dash.dashboard.application.dto.response;

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
    private List<ContributorInfo> contributors;

    public static HeatmapItem of(String date, Long count, List<ContributorInfo> contributors) {
        return new HeatmapItem(date, count, contributors);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContributorInfo {
        private Long userId;
        private String username;
        private String avatarUrl;
        private Long count;

        public static ContributorInfo of(Long userId, String username, String avatarUrl, Long count) {
            return new ContributorInfo(userId, username, avatarUrl, count);
        }
    }
}
