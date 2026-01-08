package com.ssafy.dash.solvedac.infrastructure;

import com.ssafy.dash.solvedac.domain.ClassStat;
import com.ssafy.dash.solvedac.domain.SolvedacApiClient;
import com.ssafy.dash.solvedac.domain.SolvedacUser;
import com.ssafy.dash.solvedac.domain.TagStat;

import com.ssafy.dash.solvedac.domain.exception.SolvedacApiException;
import com.ssafy.dash.solvedac.infrastructure.dto.ClassStatResponse;
import com.ssafy.dash.solvedac.infrastructure.dto.SolvedacUserResponse;
import com.ssafy.dash.solvedac.infrastructure.dto.TagStatResponse;
import com.ssafy.dash.solvedac.infrastructure.dto.SolvedacTagRatingResponse;
import com.ssafy.dash.solvedac.infrastructure.dto.SolvedacSearchResponse;
import com.ssafy.dash.solvedac.domain.TagRating;
import com.ssafy.dash.solvedac.domain.SolvedProblem;
import com.ssafy.dash.solvedac.domain.ProblemSearchResult;
import com.ssafy.dash.solvedac.domain.Top100Problem;
import com.ssafy.dash.solvedac.infrastructure.dto.Top100Response;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    public SolvedacUser getUserInfo(String handle) {
        try {
            log.debug("Fetching user info for handle: {}", handle);
            SolvedacUserResponse response = restClient.get()
                    .uri(baseUrl + "/user/show?handle={handle}", handle)
                    .retrieve()
                    .body(SolvedacUserResponse.class);
            return mapToSolvedacUser(response);
        } catch (RestClientException e) {
            log.error("Failed to fetch user info for handle: {}", handle, e);
            throw new SolvedacApiException("사용자 정보를 가져올 수 없습니다: " + handle, e);
        }
    }

    @Override
    public List<ClassStat> getClassStats(String handle) {
        try {
            log.debug("Fetching class stats for handle: {}", handle);
            ClassStatResponse[] responses = restClient.get()
                    .uri(baseUrl + "/user/class_stats?handle={handle}", handle)
                    .retrieve()
                    .body(ClassStatResponse[].class);

            if (responses == null) {
                return Collections.emptyList();
            }
            return Arrays.stream(responses)
                    .map(this::mapToClassStat)
                    .collect(Collectors.toList());
        } catch (RestClientException e) {
            log.error("Failed to fetch class stats for handle: {}", handle, e);
            throw new SolvedacApiException("클래스 통계를 가져올 수 없습니다: " + handle, e);
        }
    }

    @Override
    public TagStat getTagStats(String handle) {
        try {
            log.debug("Fetching tag stats for handle: {}", handle);
            TagStatResponse response = restClient.get()
                    .uri(baseUrl + "/user/problem_tag_stats?handle={handle}", handle)
                    .retrieve()
                    .body(TagStatResponse.class);
            return mapToTagStat(response);
        } catch (RestClientException e) {
            log.error("Failed to fetch tag stats for handle: {}", handle, e);
            throw new SolvedacApiException("태그 통계를 가져올 수 없습니다: " + handle, e);
        }
    }

    @Override
    public List<TagRating> getTagRatings(String handle) {
        try {
            log.debug("Fetching tag ratings for handle: {}", handle);
            SolvedacTagRatingResponse response = restClient.get()
                    .uri(baseUrl + "/user/tag_ratings?handle={handle}", handle)
                    .retrieve()
                    .body(SolvedacTagRatingResponse.class);
            return mapToTagRatingList(response);
        } catch (RestClientException e) {
            log.error("Failed to fetch tag ratings for handle: {}", handle, e);
            throw new SolvedacApiException("태그 판정 정보를 가져올 수 없습니다: " + handle, e);
        }
    }

    @Override
    public ProblemSearchResult searchSolvedProblems(String handle, int page) {
        try {
            log.debug("Searching solved problems for handle: {}, page: {}", handle, page);
            SolvedacSearchResponse response = restClient.get()
                    .uri(baseUrl + "/search/problem?query=s@{handle}&page={page}", handle, page)
                    .retrieve()
                    .body(SolvedacSearchResponse.class);
            return mapToProblemSearchResult(response);
        } catch (RestClientException e) {
            log.error("Failed to search problems for handle: {}", handle, e);
            throw new SolvedacApiException("문제 검색 실패: " + handle, e);
        }
    }

    // Mappers

    private SolvedacUser mapToSolvedacUser(SolvedacUserResponse dto) {
        if (dto == null)
            return null;
        return new SolvedacUser(
                dto.getHandle(),
                dto.getBio(),
                dto.getTier(),
                dto.getRating(),
                dto.getClassLevel(),
                dto.getMaxStreak(),
                dto.getProfileImageUrl(),
                dto.getSolvedCount());
    }

    private ClassStat mapToClassStat(ClassStatResponse dto) {
        if (dto == null)
            return null;
        return new ClassStat(
                dto.getClassNumber(),
                dto.getTotal(),
                dto.getTotalSolved(),
                dto.getEssentials(),
                dto.getEssentialSolved(),
                dto.getDecoration());
    }

    private TagStat mapToTagStat(TagStatResponse dto) {
        if (dto == null)
            return null;
        List<TagStat.Item> items = dto.getItems().stream()
                .map(itemDto -> new TagStat.Item(
                        new TagStat.TagInfo(
                                itemDto.getTag().getKey(),
                                itemDto.getTag().getIsMeta(),
                                itemDto.getTag().getBojTagId()),
                        itemDto.getTotal(),
                        itemDto.getSolved(),
                        itemDto.getPartial(),
                        itemDto.getTried()))
                .collect(Collectors.toList());
        return new TagStat(dto.getCount(), items);
    }

    private List<TagRating> mapToTagRatingList(SolvedacTagRatingResponse dto) {
        if (dto == null || dto.getItems() == null)
            return Collections.emptyList();
        return dto.getItems().stream()
                .map(item -> new TagRating(
                        item.getTag().getKey(),
                        item.getRating(),
                        item.getSolvedCount() == null ? 0 : item.getSolvedCount()))
                .collect(Collectors.toList());
    }

    private ProblemSearchResult mapToProblemSearchResult(SolvedacSearchResponse dto) {
        if (dto == null)
            return new ProblemSearchResult(0, Collections.emptyList());

        List<SolvedProblem> problems = dto.getItems() == null ? Collections.emptyList()
                : dto.getItems().stream()
                        .map(item -> new SolvedProblem(
                                item.getProblemId(),
                                item.getTitleKo(),
                                item.getLevel(),
                                item.getTags().stream()
                                        .map(SolvedacSearchResponse.TagInfo::getKey)
                                        .collect(Collectors.toList())))
                        .collect(Collectors.toList());

        return new ProblemSearchResult(dto.getCount(), problems);
    }

    @Override
    public List<Top100Problem> getTop100(String handle) {
        try {
            log.debug("Fetching top 100 problems for handle: {}", handle);
            Top100Response response = restClient.get()
                    .uri(baseUrl + "/user/top_100?handle={handle}", handle)
                    .retrieve()
                    .body(Top100Response.class);

            if (response == null || response.getItems() == null) {
                return Collections.emptyList();
            }

            return response.getItems().stream()
                    .map(item -> new Top100Problem(
                            item.getProblemId(),
                            item.getTitleKo(),
                            item.getLevel(),
                            item.getTags().stream()
                                    .map(Top100Response.Tag::getKey)
                                    .collect(Collectors.toList())))
                    .collect(Collectors.toList());
        } catch (RestClientException e) {
            log.error("Failed to fetch top 100 for handle: {}", handle, e);
            throw new SolvedacApiException("Top 100 문제를 가져올 수 없습니다: " + handle, e);
        }
    }

}
