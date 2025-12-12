package com.ssafy.dash.external.solvedac;

import com.ssafy.dash.external.solvedac.dto.ClassStatResponse;
import com.ssafy.dash.external.solvedac.dto.SolvedacUserResponse;
import com.ssafy.dash.external.solvedac.dto.TagStatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.util.List;

/**
 * Solved.ac API 클라이언트 구현체
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SolvedacApiClientImpl implements SolvedacApiClient {

    @Value("${solvedac.api.base-url:https://solved.ac/api/v3}")
    private String baseUrl;

    private final RestClient restClient;

    @Override
    public SolvedacUserResponse getUserInfo(String handle) {
        try {
            log.debug("Fetching user info for handle: {}", handle);
            return restClient.get()
                    .uri(baseUrl + "/user/show?handle={handle}", handle)
                    .retrieve()
                    .body(SolvedacUserResponse.class);
        } catch (RestClientException e) {
            log.error("Failed to fetch user info for handle: {}", handle, e);
            throw new SolvedacApiException("사용자 정보를 가져올 수 없습니다: " + handle, e);
        }
    }

    @Override
    public List<ClassStatResponse> getClassStats(String handle) {
        try {
            log.debug("Fetching class stats for handle: {}", handle);
            ClassStatResponse[] stats = restClient.get()
                    .uri(baseUrl + "/user/class_stats?handle={handle}", handle)
                    .retrieve()
                    .body(ClassStatResponse[].class);
            return List.of(stats != null ? stats : new ClassStatResponse[0]);
        } catch (RestClientException e) {
            log.error("Failed to fetch class stats for handle: {}", handle, e);
            throw new SolvedacApiException("클래스 통계를 가져올 수 없습니다: " + handle, e);
        }
    }

    @Override
    public TagStatResponse getTagStats(String handle) {
        try {
            log.debug("Fetching tag stats for handle: {}", handle);
            return restClient.get()
                    .uri(baseUrl + "/user/problem_tag_stats?handle={handle}", handle)
                    .retrieve()
                    .body(TagStatResponse.class);
        } catch (RestClientException e) {
            log.error("Failed to fetch tag stats for handle: {}", handle, e);
            throw new SolvedacApiException("태그 통계를 가져올 수 없습니다: " + handle, e);
        }
    }

    @Override
    public com.ssafy.dash.external.solvedac.dto.Top100Response getTop100Problems(String handle) {
        try {
            log.debug("Fetching top 100 problems for handle: {}", handle);
            return restClient.get()
                    .uri(baseUrl + "/user/top_100?handle={handle}", handle)
                    .retrieve()
                    .body(com.ssafy.dash.external.solvedac.dto.Top100Response.class);
        } catch (RestClientException e) {
            log.error("Failed to fetch top 100 problems for handle: {}", handle, e);
            throw new SolvedacApiException("상위 100개 문제를 가져올 수 없습니다: " + handle, e);
        }
    }
}
