package com.ssafy.dash.problem.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.problem.domain.Tag;
import com.ssafy.dash.problem.domain.TagFamily;
import com.ssafy.dash.problem.infrastructure.persistence.TagMapper;
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
public class TagService {

    private final TagMapper tagMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public void initializeTags() {
        // 이미 태그 데이터가 있으면 스킵
        if (!tagMapper.findAllTags().isEmpty()) {
            log.info("태그 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        initializeFamilies();
        loadTagsFromJson();
    }

    private void initializeFamilies() {
        String[][] families = {
                { "IMPLEMENTATION", "구현", "1" },
                { "DP", "다이나믹 프로그래밍", "2" },
                { "GRAPH", "그래프", "3" },
                { "MATH", "수학", "4" },
                { "GREEDY", "그리디", "5" },
                { "STRING", "문자열", "6" },
                { "DATA_STRUCTURE", "자료구조", "7" },
                { "ADVANCED", "고급 알고리즘", "8" }
        };

        for (String[] fam : families) {
            TagFamily family = TagFamily.create(fam[0], fam[1], Integer.parseInt(fam[2]));
            try {
                tagMapper.saveTagFamily(family); // Correct method name
            } catch (Exception e) {
                log.warn("Family already exists: {}", fam[0]);
            }
        }
    }

    private void loadTagsFromJson() {
        try (var inputStream = new ClassPathResource("list.json").getInputStream()) {
            JsonNode root = objectMapper.readTree(inputStream);
            JsonNode items = root.get("items");

            for (JsonNode item : items) {
                String key = item.get("key").asText();
                int bojTagId = item.get("bojTagId").asInt();

                // displayNames에서 한국어 이름만 추출
                String koreanName = "";
                JsonNode displayNames = item.get("displayNames");
                for (JsonNode dn : displayNames) {
                    if ("ko".equals(dn.get("language").asText())) {
                        koreanName = dn.get("name").asText();
                        break;
                    }
                }

                TagMetadata metadata = analyzeTag(key);

                Long familyId = null;
                if (metadata.familyKey != null) {
                    TagFamily family = tagMapper.findFamilyByKey(metadata.familyKey);
                    if (family != null) {
                        familyId = family.getId();
                    }
                }

                Tag tag = Tag.create(
                        key,
                        bojTagId,
                        familyId,
                        metadata.parentKey,
                        metadata.tier,
                        metadata.weight,
                        "S".equals(metadata.tier) || "A".equals(metadata.tier),
                        koreanName);

                tagMapper.saveTag(tag);
            }
        } catch (IOException e) {
            log.error("JSON 파일에서 태그 로드 실패", e);
        }
    }

    private TagMetadata analyzeTag(String key) {
        // TIER S - Implementation
        if (isOneOf(key, "implementation", "simulation", "bruteforcing", "ad_hoc", "recursion"))
            return new TagMetadata("IMPLEMENTATION", "S", 3.0, null);

        // TIER S - Math
        if (isOneOf(key, "math", "arithmetic", "number_theory", "combinatorics"))
            return new TagMetadata("MATH", "S", 3.0, null);

        // TIER S - DP
        if (isOneOf(key, "dp", "knapsack"))
            return new TagMetadata("DP", "S", 3.0, null);

        // TIER S - Graph
        if (isOneOf(key, "graphs", "graph_traversal", "bfs", "dfs"))
            return new TagMetadata("GRAPH", "S", 3.0, null);

        // TIER S - Greedy
        if (key.equals("greedy"))
            return new TagMetadata("GREEDY", "S", 3.0, null);

        // TIER S - String
        if (key.equals("string"))
            return new TagMetadata("STRING", "S", 3.0, null);

        // TIER S - Data Structure
        if (isOneOf(key, "data_structures", "stack", "queue", "priority_queue", "deque"))
            return new TagMetadata("DATA_STRUCTURE", "S", 3.0, null);

        // TIER A - Implementation
        if (isOneOf(key, "case_work", "constructive", "sorting"))
            return new TagMetadata("IMPLEMENTATION", "A", 2.0, null);

        // TIER A - Math
        if (isOneOf(key, "euclidean", "primality_test", "sieve", "probability"))
            return new TagMetadata("MATH", "A", 2.0, null);

        // TIER A - DP
        if (isOneOf(key, "dp_tree", "dp_bitfield", "lis"))
            return new TagMetadata("DP", "A", 2.0, null);

        // TIER A - Graph
        if (isOneOf(key, "shortest_path", "dijkstra", "floyd_warshall", "topological_sorting", "mst", "trees"))
            return new TagMetadata("GRAPH", "A", 2.0, null);

        // TIER A - String
        if (isOneOf(key, "kmp", "trie", "hashing", "parsing"))
            return new TagMetadata("STRING", "A", 2.0, null);

        // TIER A - Data Structure
        if (isOneOf(key, "segtree", "disjoint_set", "binary_search", "set", "hash_set", "tree_set"))
            return new TagMetadata("DATA_STRUCTURE", "A", 2.0, null);

        // TIER B - Advanced
        if (isOneOf(key, "divide_and_conquer", "two_pointer", "prefix_sum", "sweeping", "backtracking",
                "bitmask", "sliding_window", "offline_queries", "parametric_search"))
            return new TagMetadata("ADVANCED", "B", 1.0, null);

        // TIER A - Graph (추가) - 정말 중요한 그래프 알고리즘
        if (isOneOf(key, "bellman_ford", "lca", "scc"))
            return new TagMetadata("GRAPH", "A", 2.0, null);

        // TIER B - 핵심만 (최소한)
        if (isOneOf(key, "flood_fill", "dp_digit", "bipartite_matching", "flow", "geometry"))
            return new TagMetadata("ADVANCED", "B", 1.0, null);

        // Default to Tier C / Advanced
        return new TagMetadata("ADVANCED", "C", 0.5, null);
    }

    private boolean isOneOf(String key, String... candidates) {
        for (String candidate : candidates) {
            if (key.equals(candidate))
                return true;
        }
        return false;
    }

    private static class TagMetadata {
        String familyKey;
        String tier;
        Double weight;
        String parentKey;

        TagMetadata(String familyKey, String tier, Double weight, String parentKey) {
            this.familyKey = familyKey;
            this.tier = tier;
            this.weight = weight;
            this.parentKey = parentKey;
        }
    }
}
