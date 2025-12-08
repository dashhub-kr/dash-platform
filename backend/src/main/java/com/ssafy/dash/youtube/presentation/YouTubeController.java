package com.ssafy.dash.youtube.presentation;

import com.ssafy.dash.youtube.application.YouTubeService;
import com.ssafy.dash.youtube.presentation.dto.response.YouTubeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/youtube")
public class YouTubeController {

    private final YouTubeService youTubeService;

    public YouTubeController(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<YouTubeResponse>> search(@RequestParam String keyword) {
        List<YouTubeResponse> responses = youTubeService.searchVideos(keyword).stream()
                .map(YouTubeResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
}
