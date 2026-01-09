package com.ssafy.dash.problem.application;

import com.ssafy.dash.analytics.domain.UserTagStat;
import com.ssafy.dash.analytics.infrastructure.persistence.UserTagStatMapper;
import com.ssafy.dash.problem.domain.Tag;
import com.ssafy.dash.problem.domain.TagFamily;
import com.ssafy.dash.problem.infrastructure.persistence.TagMapper;
import com.ssafy.dash.problem.presentation.dto.SkillTreeNode;
import com.ssafy.dash.problem.presentation.dto.SkillTreeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 스킬 트리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkillTreeService {

        private final TagMapper tagMapper;
        private final UserTagStatMapper userTagStatMapper;

        // 태그 키 -> 한글 이름 매핑
        private static final Map<String, String> TAG_NAMES = Map.ofEntries(
                        Map.entry("implementation", "구현"),
                        Map.entry("math", "수학"),
                        Map.entry("dp", "다이나믹 프로그래밍"),
                        Map.entry("data_structures", "자료 구조"),
                        Map.entry("graphs", "그래프 이론"),
                        Map.entry("greedy", "그리디 알고리즘"),
                        Map.entry("string", "문자열"),
                        Map.entry("bruteforcing", "브루트포스"),
                        Map.entry("sorting", "정렬"),
                        Map.entry("bfs", "너비 우선 탐색"),
                        Map.entry("dfs", "깊이 우선 탐색"),
                        Map.entry("binary_search", "이분 탐색"),
                        Map.entry("simulation", "시뮬레이션"),
                        Map.entry("number_theory", "정수론"),
                        Map.entry("graph_traversal", "그래프 탐색"),
                        Map.entry("geometry", "기하학"),
                        Map.entry("trees", "트리"),
                        Map.entry("backtracking", "백트래킹"),
                        Map.entry("recursion", "재귀"),
                        Map.entry("combinatorics", "조합론"),
                        Map.entry("stack", "스택"),
                        Map.entry("queue", "큐"),
                        Map.entry("priority_queue", "우선순위 큐"),
                        Map.entry("deque", "덱"),
                        Map.entry("segtree", "세그먼트 트리"),
                        Map.entry("disjoint_set", "분리 집합"),
                        Map.entry("shortest_path", "최단 경로"),
                        Map.entry("dijkstra", "다익스트라"),
                        Map.entry("floyd_warshall", "플로이드-워셜"),
                        Map.entry("mst", "최소 신장 트리"),
                        Map.entry("topological_sorting", "위상 정렬"),
                        Map.entry("two_pointer", "투 포인터"),
                        Map.entry("prefix_sum", "누적 합"),
                        Map.entry("divide_and_conquer", "분할 정복"),
                        Map.entry("sliding_window", "슬라이딩 윈도우"),
                        Map.entry("knapsack", "배낭 문제"),
                        Map.entry("lis", "최장 증가 부분 수열"),
                        Map.entry("dp_tree", "트리 DP"),
                        Map.entry("dp_bitfield", "비트필드 DP"),
                        Map.entry("kmp", "KMP"),
                        Map.entry("trie", "트라이"),
                        Map.entry("hashing", "해싱"),
                        Map.entry("parsing", "파싱"));

        /**
         * 사용자 스킬 트리 조회
         */
        public SkillTreeResponse getSkillTree(Long userId) {
                // 1. 모든 Family 조회
                List<TagFamily> families = tagMapper.findAllFamilies();

                // 2. 모든 태그 조회
                List<Tag> allTags = tagMapper.findAllTags();

                // 3. 사용자 태그 통계 조회
                List<UserTagStat> userStats = userTagStatMapper.findByUserId(userId);
                Map<String, UserTagStat> userStatsMap = userStats.stream()
                                .collect(Collectors.toMap(UserTagStat::getTagKey, s -> s, (a, b) -> a));

                // 4. Family별로 태그 그룹핑
                Map<Long, List<Tag>> tagsByFamily = allTags.stream()
                                .filter(t -> t.getFamilyId() != null)
                                .collect(Collectors.groupingBy(Tag::getFamilyId));

                // 5. 스킬 트리 노드 생성
                List<SkillTreeNode> familyNodes = families.stream()
                                .sorted(Comparator.comparing(TagFamily::getOrderIndex))
                                .map(family -> {
                                        List<Tag> familyTags = tagsByFamily.getOrDefault(family.getId(),
                                                        Collections.emptyList());

                                        List<SkillTreeNode> children = familyTags.stream()
                                                        // S, A, B, C 티어 모두 표시
                                                        // .filter(tag -> !"C".equals(tag.getImportanceTier()))
                                                        .map(tag -> buildTagNode(tag,
                                                                        userStatsMap.get(tag.getTagKey())))
                                                        .sorted(Comparator.comparing(SkillTreeNode::getTier)
                                                                        .thenComparing(n -> n.getSolved() == null ? 0
                                                                                        : -n.getSolved()))
                                                        .collect(Collectors.toList());

                                        // Family 노드의 전체 진행도 계산
                                        int totalSolved = children.stream()
                                                        .mapToInt(n -> n.getSolved() != null ? n.getSolved() : 0)
                                                        .sum();
                                        int totalTags = children.size();
                                        double avgProgress = children.stream()
                                                        .mapToDouble(n -> n.getProgressPercent() != null
                                                                        ? n.getProgressPercent()
                                                                        : 0)
                                                        .average()
                                                        .orElse(0);

                                        // 평균 푼 문제 수로 마스터리 레벨 계산 (UserTagStat과 동일 로직)
                                        double avgSolved = totalTags > 0 ? (double) totalSolved / totalTags : 0;
                                        String familyMastery = getMasteryLevelBySolved((int) avgSolved);

                                        return SkillTreeNode.builder()
                                                        .key(family.getFamilyKey())
                                                        .name(family.getName())
                                                        .familyKey(null) // 최상위 노드
                                                        .tier("ROOT")
                                                        .isCore(true)
                                                        .solved(totalSolved)
                                                        .total(totalTags * 10) // 태그당 10문제 기준
                                                        .progressPercent(avgProgress)
                                                        .masteryLevel(familyMastery)
                                                        .children(children)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                // 6. 전체 통계 계산
                int totalTags = (int) familyNodes.stream()
                                .flatMap(f -> f.getChildren().stream())
                                .count();

                int masteredTags = (int) familyNodes.stream()
                                .flatMap(f -> f.getChildren().stream())
                                .filter(n -> n.getSolved() != null && n.getSolved() >= 10)
                                .count();

                int learningTags = (int) familyNodes.stream()
                                .flatMap(f -> f.getChildren().stream())
                                .filter(n -> n.getSolved() != null && n.getSolved() > 0 && n.getSolved() < 10)
                                .count();

                int lockedTags = totalTags - masteredTags - learningTags;

                double overallProgress = totalTags > 0 ? familyNodes.stream()
                                .flatMap(f -> f.getChildren().stream())
                                .mapToDouble(n -> n.getProgressPercent() != null ? n.getProgressPercent() : 0)
                                .average()
                                .orElse(0) : 0;

                return SkillTreeResponse.builder()
                                .families(familyNodes)
                                .totalTags(totalTags)
                                .masteredTags(masteredTags)
                                .learningTags(learningTags)
                                .lockedTags(lockedTags)
                                .overallProgress(Math.round(overallProgress * 10) / 10.0)
                                .build();
        }

        private SkillTreeNode buildTagNode(Tag tag, UserTagStat userStat) {
                int solved = userStat != null ? userStat.getSolved() : 0;
                int total = userStat != null && userStat.getTotal() != null ? userStat.getTotal() : 10;
                double progress = total > 0 ? Math.min(100, (double) solved / total * 100) : 0;

                // UserTagStat의 마스터리 레벨 사용 (푼 문제 수 기반)
                String masteryLevel = userStat != null ? userStat.getMasteryLevel() : "NONE";

                return SkillTreeNode.builder()
                                .key(tag.getTagKey())
                                .name(getTagName(tag))
                                .familyKey(null) // Family ID는 부모에서 관리
                                .tier(tag.getImportanceTier())
                                .isCore(tag.getIsCore())
                                .bojTagId(tag.getBojTagId())
                                .solved(solved)
                                .total(total)
                                .progressPercent(Math.round(progress * 10) / 10.0)
                                .masteryLevel(masteryLevel)
                                .children(null) // 리프 노드
                                .build();
        }

        private String getTagName(Tag tag) {
                // 먼저 태그의 displayNames 확인
                if (tag.getDisplayNames() != null && !tag.getDisplayNames().isEmpty()) {
                        return tag.getDisplayNames();
                }
                // 없으면 매핑 테이블에서 조회
                return TAG_NAMES.getOrDefault(tag.getTagKey(), tag.getTagKey());
        }

        /**
         * 푼 문제 수 기반 마스터리 레벨 (UserTagStat과 동일 로직)
         */
        private String getMasteryLevelBySolved(int solved) {
                if (solved >= 50)
                        return "MASTER";
                if (solved >= 30)
                        return "EXPERT";
                if (solved >= 15)
                        return "ADVANCED";
                if (solved >= 5)
                        return "INTERMEDIATE";
                if (solved >= 1)
                        return "BEGINNER";
                return "NONE";
        }
}
