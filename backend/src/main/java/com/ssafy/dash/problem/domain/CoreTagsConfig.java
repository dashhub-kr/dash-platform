package com.ssafy.dash.problem.domain;

import java.util.Map;
import java.util.Set;

/**
 * 코딩테스트 태그 설정 (Single Source of Truth)
 * 
 * [Priority 매핑 규칙 - 프로그래머스 고득점 Kit 기준]
 * - Priority 1 (빈출: 높음) -> Tier S (Weight 3.0)
 * - Priority 2 (빈출: 보통) -> Tier A (Weight 2.0)
 * - Priority 3 (빈출: 낮음) -> Tier B+ (Weight 1.5)
 * - Priority 4 (Kit 미포함) -> Tier B (Weight 1.0)
 */
public final class CoreTagsConfig {

    private CoreTagsConfig() {
    }

    public record TagMetadata(
            String familyKey,
            String tier,
            double weight,
            boolean isCore,
            boolean isBasic, // 초보자용 기초 태그 여부
            int priority) {
    }

    public static TagMetadata getMetadata(String tagKey) {
        String familyKey = FAMILY_MAP.getOrDefault(tagKey, "ADVANCED");
        Integer priority = PRIORITY_MAP.get(tagKey);
        boolean isBasic = BASIC_TAGS.contains(tagKey);

        if (priority == null) {
            // Priority가 없으면 -> Non-Core, Tier B
            return new TagMetadata(familyKey, "B", 1.0, false, isBasic, 99);
        }

        String tier;
        double weight;

        // 프로그래머스 빈출도 기반 중요도 산정
        switch (priority) {
            case 1: // 높음
                tier = "S";
                weight = 3.0;
                break;
            case 2: // 보통
                tier = "A";
                weight = 2.0;
                break;
            case 3: // 낮음
                tier = "B+";
                weight = 1.5;
                break;
            default: // 그 외 (심화 등)
                tier = "B";
                weight = 1.0;
                break;
        }

        return new TagMetadata(familyKey, tier, weight, true, isBasic, priority);
    }

    /**
     * Strategy: 기초 태그 (Bronze 이하 추천용)
     * - Implementation, Boolean, String, Math 등 기본적인 문제 해결 능력을 요구하는 태그
     */
    private static final Set<String> BASIC_TAGS = Set.of(
            "implementation", "math", "arithmetic", "string",
            "bruteforcing", "sorting", "simulation");

    /**
     * Taxonomy: 태그별 패밀리 분류 (Fact)
     */
    private static final Map<String, String> FAMILY_MAP = Map.ofEntries(
            // IMPLEMENTATION
            Map.entry("implementation", "IMPLEMENTATION"),
            Map.entry("simulation", "IMPLEMENTATION"),
            Map.entry("bruteforcing", "IMPLEMENTATION"),
            Map.entry("ad_hoc", "IMPLEMENTATION"),
            Map.entry("recursion", "IMPLEMENTATION"),
            Map.entry("case_work", "IMPLEMENTATION"),
            Map.entry("constructive", "IMPLEMENTATION"),
            Map.entry("sorting", "IMPLEMENTATION"),

            // MATH
            Map.entry("math", "MATH"),
            Map.entry("arithmetic", "MATH"),
            Map.entry("number_theory", "MATH"),
            Map.entry("combinatorics", "MATH"),
            Map.entry("euclidean", "MATH"),
            Map.entry("primality_test", "MATH"),
            Map.entry("sieve", "MATH"),
            Map.entry("probability", "MATH"),

            // DP
            Map.entry("dp", "DP"),
            Map.entry("dynamic_programming", "DP"),
            Map.entry("knapsack", "DP"),
            Map.entry("dp_tree", "DP"),
            Map.entry("dp_bitfield", "DP"),
            Map.entry("lis", "DP"),
            Map.entry("dp_digit", "DP"),

            // GRAPH
            Map.entry("graphs", "GRAPH"),
            Map.entry("graph_traversal", "GRAPH"),
            Map.entry("bfs", "GRAPH"),
            Map.entry("dfs", "GRAPH"),
            Map.entry("shortest_path", "GRAPH"),
            Map.entry("dijkstra", "GRAPH"),
            Map.entry("floyd_warshall", "GRAPH"),
            Map.entry("topological_sorting", "GRAPH"),
            Map.entry("mst", "GRAPH"),
            Map.entry("trees", "GRAPH"),
            Map.entry("bellman_ford", "GRAPH"),
            Map.entry("lca", "GRAPH"),
            Map.entry("scc", "GRAPH"),

            // GREEDY
            Map.entry("greedy", "GREEDY"),

            // STRING
            Map.entry("string", "STRING"),
            Map.entry("kmp", "STRING"),
            Map.entry("trie", "STRING"),
            Map.entry("hashing", "STRING"),
            Map.entry("parsing", "STRING"),

            // DATA_STRUCTURE
            Map.entry("data_structures", "DATA_STRUCTURE"),
            Map.entry("stack", "DATA_STRUCTURE"),
            Map.entry("queue", "DATA_STRUCTURE"),
            Map.entry("priority_queue", "DATA_STRUCTURE"),
            Map.entry("deque", "DATA_STRUCTURE"),
            Map.entry("segtree", "DATA_STRUCTURE"),
            Map.entry("disjoint_set", "DATA_STRUCTURE"),
            Map.entry("binary_search", "DATA_STRUCTURE"),
            Map.entry("set", "DATA_STRUCTURE"),
            Map.entry("hash_set", "DATA_STRUCTURE"),
            Map.entry("tree_set", "DATA_STRUCTURE"),

            // ADVANCED (기타)
            Map.entry("divide_and_conquer", "ADVANCED"),
            Map.entry("two_pointer", "ADVANCED"),
            Map.entry("prefix_sum", "ADVANCED"),
            Map.entry("sweeping", "ADVANCED"),
            Map.entry("backtracking", "ADVANCED"),
            Map.entry("bitmask", "ADVANCED"),
            Map.entry("sliding_window", "ADVANCED"),
            Map.entry("offline_queries", "ADVANCED"),
            Map.entry("parametric_search", "ADVANCED"),
            Map.entry("flood_fill", "ADVANCED"),
            Map.entry("bipartite_matching", "ADVANCED"),
            Map.entry("flow", "ADVANCED"),
            Map.entry("geometry", "ADVANCED"));

    /**
     * Strategy: 프로그래머스 고득점 Kit 출제 빈도
     */
    private static final Map<String, Integer> PRIORITY_MAP = Map.ofEntries(
            // ===================================
            // Priority 1: 출제 빈도 [높음]
            // ===================================
            Map.entry("hash_set", 1), // 해시
            Map.entry("hashing", 1),
            Map.entry("sorting", 1), // 정렬
            Map.entry("bruteforcing", 1), // 완전탐색
            Map.entry("bfs", 1), // DFS/BFS
            Map.entry("dfs", 1),
            Map.entry("graph_traversal", 1),

            // ===================================
            // Priority 2: 출제 빈도 [보통]
            // ===================================
            Map.entry("stack", 2), // 스택/큐
            Map.entry("queue", 2),
            Map.entry("deque", 2),
            Map.entry("priority_queue", 2), // 힙(Heap)
            Map.entry("greedy", 2), // 탐욕법
            Map.entry("dp", 2), // 동적계획법
            Map.entry("dynamic_programming", 2),

            // ===================================
            // Priority 3: 출제 빈도 [낮음]
            // ===================================
            Map.entry("binary_search", 3), // 이분탐색
            Map.entry("graphs", 3), // 그래프
            Map.entry("shortest_path", 3), // (그래프 세부)
            Map.entry("dijkstra", 3), // (그래프 세부)

            // ===================================
            // Priority 4: Kit 미포함 (심화/기타)
            // ===================================
            Map.entry("implementation", 4),
            Map.entry("simulation", 4),
            Map.entry("two_pointer", 4),
            Map.entry("sliding_window", 4),
            Map.entry("prefix_sum", 4),
            Map.entry("backtracking", 4),
            Map.entry("trees", 4),
            Map.entry("topological_sorting", 4),
            Map.entry("disjoint_set", 4),
            Map.entry("recursion", 4));

    // =============================================================
    // Utility Methods
    // =============================================================

    public static boolean isCoreTag(String tagKey) {
        return PRIORITY_MAP.containsKey(tagKey);
    }

    public static boolean isBasicTag(String tagKey) {
        return BASIC_TAGS.contains(tagKey);
    }

    public static int getPriority(String tagKey) {
        return getMetadata(tagKey).priority();
    }
}
