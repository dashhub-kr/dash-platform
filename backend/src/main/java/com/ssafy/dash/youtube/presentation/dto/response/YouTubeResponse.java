package com.ssafy.dash.youtube.presentation.dto.response;

import com.ssafy.dash.youtube.domain.YouTubeVideo;
import java.time.LocalDateTime;

public record YouTubeResponse(
        String videoId,
        String title,
        String description,
        String thumbnailUrl,
        String channelTitle,
        LocalDateTime publishedAt) {
    public static YouTubeResponse from(YouTubeVideo video) {
        return new YouTubeResponse(
                video.videoId(),
                video.title(),
                video.description(),
                video.thumbnailUrl(),
                video.channelTitle(),
                video.publishedAt());
    }
}
