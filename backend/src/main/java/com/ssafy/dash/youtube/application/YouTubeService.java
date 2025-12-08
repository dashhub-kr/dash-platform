package com.ssafy.dash.youtube.application;

import com.ssafy.dash.youtube.domain.YouTubeClient;
import com.ssafy.dash.youtube.domain.YouTubeVideo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class YouTubeService {

    private final YouTubeClient youTubeClient;

    public YouTubeService(YouTubeClient youTubeClient) {
        this.youTubeClient = youTubeClient;
    }

    public List<YouTubeVideo> searchVideos(String keyword) {
        return youTubeClient.searchVideos(keyword);
    }
}
