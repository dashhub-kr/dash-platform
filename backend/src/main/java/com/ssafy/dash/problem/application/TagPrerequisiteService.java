package com.ssafy.dash.problem.application;

import com.ssafy.dash.problem.domain.Tag;
import com.ssafy.dash.problem.domain.TagPrerequisite;
import com.ssafy.dash.problem.infrastructure.persistence.TagMapper;
import com.ssafy.dash.problem.infrastructure.persistence.TagPrerequisiteMapper;
import com.ssafy.dash.problem.presentation.dto.SkillGraphResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 태그 선수 관계 서비스 (그래프 스킬트리용)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagPrerequisiteService {

    private final TagPrerequisiteMapper prerequisiteMapper;
    private final TagMapper tagMapper;

    /**
     * 애플리케이션 시작 시 선수 관계 데이터 자동 초기화
     */
    @PostConstruct
    public void init() {
        try {
            List<TagPrerequisite> existing = prerequisiteMapper.findAll();
            if (existing.isEmpty()) {
                initializeDefaultPrerequisites();
                log.info("Tag prerequisites initialized on startup");
            } else {
                log.info("Tag prerequisites already exist ({} entries), skipping initialization", existing.size());
            }
        } catch (Exception e) {
            log.warn("Failed to initialize tag prerequisites: {}", e.getMessage());
        }
    }

    /**
     * 그래프 형태로 스킬트리 조회
     */
    public SkillGraphResponse getSkillGraph(Long userId) {
        List<Tag> allTags = tagMapper.findAllTags();
        List<TagPrerequisite> allPrerequisites = prerequisiteMapper.findAll();

        // 노드 생성
        List<SkillGraphResponse.Node> nodes = allTags.stream()
                .filter(t -> !"C".equals(t.getImportanceTier())) // C티어 제외
                .map(tag -> SkillGraphResponse.Node.builder()
                        .id(tag.getTagKey())
                        .label(tag.getDisplayNames() != null ? tag.getDisplayNames() : tag.getTagKey())
                        .tier(tag.getImportanceTier())
                        .familyId(tag.getFamilyId())
                        .build())
                .collect(Collectors.toList());

        // 엣지 생성
        List<SkillGraphResponse.Edge> edges = allPrerequisites.stream()
                .map(prereq -> SkillGraphResponse.Edge.builder()
                        .source(prereq.getPrerequisiteTagKey())
                        .target(prereq.getTagKey())
                        .strength(prereq.getStrength())
                        .build())
                .collect(Collectors.toList());

        return SkillGraphResponse.builder()
                .nodes(nodes)
                .edges(edges)
                .build();
    }

    /**
     * 모든 선수 관계 조회
     */
    public List<TagPrerequisite> findAll() {
        return prerequisiteMapper.findAll();
    }

    /**
     * 특정 태그의 선수 태그들 조회
     */
    public List<TagPrerequisite> findPrerequisitesFor(String tagKey) {
        return prerequisiteMapper.findByTagKey(tagKey);
    }

    /**
     * 초기 선수 관계 데이터 삽입 (개발용)
     */
    public void initializeDefaultPrerequisites() {
        prerequisiteMapper.deleteAll(); // 기존 데이터 초기화
        List<TagPrerequisite> defaults = getDefaultPrerequisites();
        prerequisiteMapper.insertBatch(defaults);
        log.info("Initialized {} tag prerequisites", defaults.size());
    }

    /**
     * 기본 알고리즘 학습 순서 정의
     */
    private List<TagPrerequisite> getDefaultPrerequisites() {
        List<TagPrerequisite> list = new ArrayList<>();

        // DP 계열
        add(list, "dp", "bruteforcing", 2); // DP 전에 완전탐색 필수
        add(list, "dp", "recursion", 1); // 재귀 권장
        add(list, "knapsack", "dp", 2); // 배낭 전에 DP 필수
        add(list, "lis", "dp", 2); // LIS 전에 DP 필수
        add(list, "dp_tree", "dp", 2); // 트리DP 전에 DP 필수
        add(list, "dp_tree", "trees", 2); // 트리DP 전에 트리 필수
        add(list, "dp_bitfield", "dp", 2); // 비트DP 전에 DP 필수

        // 그래프 계열
        add(list, "bfs", "graphs", 2); // BFS 전에 그래프 필수
        add(list, "dfs", "graphs", 2); // DFS 전에 그래프 필수
        add(list, "graph_traversal", "bfs", 1); // 그래프 탐색 전에 BFS 권장
        add(list, "graph_traversal", "dfs", 1); // 그래프 탐색 전에 DFS 권장
        add(list, "dijkstra", "bfs", 2); // 다익스트라 전에 BFS 필수
        add(list, "dijkstra", "priority_queue", 2); // 다익스트라 전에 우선순위큐 필수
        add(list, "shortest_path", "dijkstra", 1); // 최단경로 전에 다익스트라 권장
        add(list, "floyd_warshall", "graphs", 2); // 플로이드 전에 그래프 필수
        add(list, "mst", "graphs", 2); // MST 전에 그래프 필수
        add(list, "mst", "greedy", 1); // MST 전에 그리디 권장
        add(list, "topological_sorting", "dfs", 2); // 위상정렬 전에 DFS 필수
        add(list, "trees", "graphs", 2); // 트리 전에 그래프 필수

        // 자료구조 계열
        add(list, "stack", "data_structures", 2); // 스택 전에 자료구조 필수
        add(list, "queue", "data_structures", 2); // 큐 전에 자료구조 필수
        add(list, "deque", "data_structures", 2); // 덱 전에 자료구조 필수
        add(list, "priority_queue", "queue", 2); // 우선순위큐 전에 큐 필수
        add(list, "priority_queue", "sorting", 1); // 힙 전에 정렬 권장

        add(list, "bfs", "queue", 2); // BFS 구현에 큐 필수
        add(list, "dfs", "stack", 1); // DFS 개념 이해에 스택 권장 (재귀로 대체 가능하므로 1)

        add(list, "segtree", "divide_and_conquer", 2);// 세그트리 전에 분할정복 필수
        add(list, "segtree", "trees", 1); // 세그트리 전에 트리 권장
        add(list, "disjoint_set", "trees", 1); // 분리집합 전에 트리 권장

        // 탐색/정렬 계열
        add(list, "binary_search", "sorting", 2); // 이분탐색 전에 정렬 필수
        add(list, "two_pointer", "sorting", 1); // 투포인터 전에 정렬 권장
        add(list, "sliding_window", "two_pointer", 1);// 슬라이딩 전에 투포인터 권장
        add(list, "divide_and_conquer", "recursion", 2);// 분할정복 전에 재귀 필수

        // 문자열 계열
        add(list, "kmp", "string", 2); // KMP 전에 문자열 필수
        add(list, "trie", "string", 2); // 트라이 전에 문자열 필수
        add(list, "trie", "trees", 1); // 트라이 전에 트리 권장

        // 기초
        add(list, "backtracking", "recursion", 2); // 백트래킹 전에 재귀 필수
        add(list, "backtracking", "bruteforcing", 1); // 백트래킹 전에 완전탐색 권장
        add(list, "simulation", "implementation", 1); // 시뮬레이션 전에 구현 권장
        add(list, "number_theory", "math", 2); // 정수론 전에 수학 필수
        add(list, "combinatorics", "math", 2); // 조합론 전에 수학 필수
        add(list, "geometry", "math", 2); // 기하학 전에 수학 필수
        add(list, "probability", "math", 2); // 확률론 전에 수학 필수
        add(list, "primality_test", "number_theory", 2); // 소수판정 전에 정수론 필수
        add(list, "sieve", "number_theory", 2); // 에라토스테네스의 체 전에 정수론 필수
        add(list, "euclidean", "number_theory", 2); // 유클리드 전에 정수론 필수

        // 그래프 심화
        add(list, "mst", "disjoint_set", 2); // MST(크루스칼) 전에 분리집합 필수
        add(list, "scc", "dfs", 2); // SCC 전에 DFS 필수
        add(list, "lca", "trees", 2); // LCA 전에 트리 필수
        add(list, "lca", "dfs", 1); // LCA 전에 DFS 권장
        add(list, "bellman_ford", "graphs", 2); // 벨만포드 전에 그래프 필수
        add(list, "floyd_warshall", "dp", 1); // 플로이드 전에 DP 개념 권장
        add(list, "bipartite_matching", "dfs", 2); // 이분매칭 전에 DFS(또는 BFS) 필수
        add(list, "mf", "graphs", 2); // 최대유량 전에 그래프 필수
        add(list, "mf", "bfs", 2); // 최대유량(에드몬드카프) 전에 BFS 필수
        add(list, "mcmf", "mf", 2); // MCMF 전에 최대유량 필수
        add(list, "mcmf", "bellman_ford", 1); // MCMF 전에 벨만포드/SPFA 권장

        // 자료구조 & 기타
        add(list, "hash_set", "data_structures", 2); // 해시셋 전에 자료구조 필수
        add(list, "tree_set", "data_structures", 2); // 트리셋 전에 자료구조 필수
        add(list, "tree_set", "trees", 2); // 트리셋 전에 트리 필수
        add(list, "dp_bitfield", "bitmask", 2); // 비트DP 전에 비트마스크 필수
        add(list, "convex_hull", "geometry", 2); // 볼록껍질 전에 기하학 필수

        return list;
    }

    private void add(List<TagPrerequisite> list, String tagKey, String prereqKey, int strength) {
        list.add(TagPrerequisite.create(tagKey, prereqKey, strength));
    }
}
