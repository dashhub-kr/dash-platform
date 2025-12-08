package com.ssafy.dash.youtube.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.youtube.config.YouTubeProperties;
import com.ssafy.dash.youtube.domain.YouTubeClient;
import com.ssafy.dash.youtube.domain.YouTubeVideo;
import com.ssafy.dash.youtube.domain.exception.YouTubeException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class YouTubeClientImpl implements YouTubeClient {

    private static final String ROOT_URI = "https://www.googleapis.com/youtube/v3";
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public YouTubeClientImpl(RestTemplateBuilder restTemplateBuilder, YouTubeProperties properties,
            ObjectMapper objectMapper) {
        this.restTemplate = restTemplateBuilder.rootUri(ROOT_URI).build();
        this.apiKey = properties.getApiKey();
        this.objectMapper = objectMapper;
    }

    @Override
    public List<YouTubeVideo> searchVideos(String keyword) {
        URI uri = UriComponentsBuilder.fromUriString(ROOT_URI)
                .path("/search")
                .queryParam("part", "snippet")
                .queryParam("maxResults", 10)
                .queryParam("q", keyword)
                .queryParam("type", "video")
                .queryParam("key", apiKey)
                .build()
                .toUri();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            return parseResponse(response.getBody());
        } catch (RestClientException e) {
            throw new YouTubeException("Failed to search YouTube videos", e);
        } catch (Exception e) {
            throw new YouTubeException("Error parsing YouTube API response", e);
        }
    }

    private List<YouTubeVideo> parseResponse(String responseBody) throws Exception {
        List<YouTubeVideo> videos = new ArrayList<>();
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode items = root.path("items");

        for (JsonNode item : items) {
            String videoId = item.path("id").path("videoId").asText();
            JsonNode snippet = item.path("snippet");
            String title = snippet.path("title").asText();
            String description = snippet.path("description").asText();
            String thumbnailUrl = snippet.path("thumbnails").path("medium").path("url").asText();
            String channelTitle = snippet.path("channelTitle").asText();
            String publishedAtStr = snippet.path("publishedAt").asText(); // ISO 8601

            // Simple parsing, assuming ISO 8601 format
            LocalDateTime publishedAt = LocalDateTime.parse(publishedAtStr, DateTimeFormatter.ISO_DATE_TIME);

            videos.add(new YouTubeVideo(videoId, title, description, thumbnailUrl, channelTitle, publishedAt));
        }

        return videos;
    }
}
