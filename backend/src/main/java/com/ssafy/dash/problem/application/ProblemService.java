package com.ssafy.dash.problem.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.problem.domain.Problem;
import com.ssafy.dash.problem.domain.ProblemTag;
import com.ssafy.dash.problem.infrastructure.persistence.ProblemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemService {

    private final ProblemMapper problemMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public void initializeProblems() {
        // 이미 문제 데이터가 있으면 스킵
        if (problemMapper.findProblemByNumber("1000") != null) {
            log.info("문제 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("문제 데이터 초기화 시작...");
        try {
            File file = new ClassPathResource("problems_with_keys.json").getFile();
            JsonNode root = objectMapper.readTree(file);

            if (root.isArray()) {
                for (JsonNode node : root) {
                    processProblemNode(node);
                }
            }
            log.info("문제 데이터 초기화 완료");
        } catch (IOException e) {
            log.error("문제 데이터 로드 실패", e);
        }
    }

    private void processProblemNode(JsonNode node) {
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
}
