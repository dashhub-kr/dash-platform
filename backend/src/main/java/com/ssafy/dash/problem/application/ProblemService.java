package com.ssafy.dash.problem.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.problem.domain.Problem;
import com.ssafy.dash.problem.domain.ProblemTag;
import com.ssafy.dash.problem.infrastructure.persistence.ProblemMapper;
import com.ssafy.dash.algorithm.infrastructure.mapper.AlgorithmRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemService {

    private final ProblemMapper problemMapper;
    private final AlgorithmRecordMapper algorithmRecordMapper;
    private final ObjectMapper objectMapper;
    private final TagService tagService;

    @Transactional
    public void initializeProblems() {
        try (var inputStream = new ClassPathResource("data/problems_with_keys.json").getInputStream()) {
            JsonNode rootNode = objectMapper.readTree(inputStream);
            int jsonCount = rootNode.size();
            int dbCount = problemMapper.countProblems();

            // DB 개수와 JSON 개수가 같더라도 내용이 변경되었을 수 있으므로 항상 업데이트 시도 (UPSERT 동작)
            // if (dbCount >= jsonCount) {
            // log.info("문제 데이터가 이미 최신 상태입니다 (DB: {}, JSON: {}). 초기화를 건너뜁니다.", dbCount,
            // jsonCount);
            // return;
            // }

            log.info("문제 데이터 초기화 시작... (DB: {}, JSON: {})", dbCount, jsonCount);

            for (JsonNode node : rootNode) {
                String problemId = node.get("problemId").asText();
                String title = node.get("title").asText();
                int level = node.get("level").asInt();
                int problemClass = node.has("class") ? node.get("class").asInt(0) : 0;
                boolean essential = node.has("essential") && node.get("essential").asBoolean();
                boolean sprout = node.has("sprout") && node.get("sprout").asBoolean();

                JsonNode tagsNode = node.get("tags");

                // Problem 엔티티 저장 (tags JSON 제거)
                Problem problem = Problem.create(
                        problemId,
                        title,
                        level,
                        problemClass,
                        essential,
                        sprout);
                problemMapper.saveProblem(problem);

                // problem_tags 테이블에만 태그 관계 저장
                if (tagsNode != null && tagsNode.isArray()) {
                    for (JsonNode tag : tagsNode) {
                        String tagKey = tag.asText();
                        ProblemTag problemTag = ProblemTag.create(problemId, tagKey);
                        problemMapper.saveProblemTag(problemTag);
                    }
                }
            }
            log.info("문제 데이터 초기화 완료");

        } catch (IOException e) {
            log.error("문제 데이터 초기화 실패", e);
            throw new RuntimeException("문제 데이터 초기화 실패", e);
        }
    }

    public java.util.List<com.ssafy.dash.problem.presentation.dto.response.ProblemRecommendationResponse> getRecommendedProblems(
            String tag, int userTier, Long userId) {
        return getRecommendedProblems(tag, userTier, userId, null);
    }

    public java.util.List<com.ssafy.dash.problem.presentation.dto.response.ProblemRecommendationResponse> getRecommendedProblems(
            String tag, int userTier, Long userId, java.util.List<String> additionalExcludedIds) {
        // 난이도 범위: 내 티어 ~ 내 티어 + 2
        int minLevel = Math.max(1, userTier);
        int maxLevel = Math.min(30, userTier + 2);

        // 이미 푼 문제 목록 조회
        java.util.List<String> solvedProblemNumbers = algorithmRecordMapper.selectSolvedProblemNumbersByUserId(userId);

        // 추가 제외 문제 병합
        if (additionalExcludedIds != null && !additionalExcludedIds.isEmpty()) {
            solvedProblemNumbers.addAll(additionalExcludedIds);
        }

        java.util.List<Problem> problems = problemMapper.findProblemsByTagAndLevelRange(
                tag, minLevel, maxLevel, solvedProblemNumbers);

        // Fallback: 만약 추천된 문제가 없다면 범위를 넓혀서 재검색 (±5)
        if (problems.isEmpty()) {
            int fallbackMin = Math.max(1, userTier - 5);
            int fallbackMax = Math.min(30, userTier + 5);
            problems = problemMapper.findProblemsByTagAndLevelRange(
                    tag, fallbackMin, fallbackMax, solvedProblemNumbers);
        }

        return problems.stream()
                .map(problem -> {
                    java.util.List<String> tags = problemMapper.findTagsByProblemNumber(problem.getProblemNumber());
                    java.util.List<String> displayTags = tags.stream()
                            .map(tagService::getKoreanName)
                            .toList();
                    return com.ssafy.dash.problem.presentation.dto.response.ProblemRecommendationResponse.from(problem,
                            displayTags,
                            null);
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
