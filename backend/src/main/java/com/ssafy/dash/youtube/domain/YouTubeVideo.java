package com.ssafy.dash.youtube.domain;

import java.time.LocalDateTime;

public record YouTubeVideo(
        String videoId,
        String title,
        String description,
        String thumbnailUrl,
        String channelTitle,
        LocalDateTime publishedAt) {
}
