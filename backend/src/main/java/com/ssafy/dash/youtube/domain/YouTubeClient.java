package com.ssafy.dash.youtube.domain;

import java.util.List;

public interface YouTubeClient {
    List<YouTubeVideo> searchVideos(String keyword);
}
