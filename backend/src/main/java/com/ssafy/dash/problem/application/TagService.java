package com.ssafy.dash.problem.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.problem.domain.Tag;
import com.ssafy.dash.problem.domain.TagFamily;
import com.ssafy.dash.problem.domain.CoreTagsConfig;
import com.ssafy.dash.problem.infrastructure.persistence.TagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {

    private final TagMapper tagMapper;
    private final ObjectMapper objectMapper;
    private final java.util.Map<String, String> tagNameCache = new java.util.concurrent.ConcurrentHashMap<>();

    @Transactional
    public void initializeTags() {
        // 태그 데이터가 있어도 메타데이터/표시이름 동기화를 위해 실행
        log.info("태그 초기화 및 메타데이터 동기화 시작...");

        try {
            initializeFamilies();
        } catch (Exception e) {
            log.warn("태그 패밀리 초기화 중 오류 (무시 가능): {}", e.getMessage());
        }

        loadTagsFromJson(); // DB Upsert (Fixes truncated display_names)
        syncTagMetadata(); // Cache update & Consistency check
    }

    /**
     * CoreTagsConfig 기반으로 DB의 태그 메타데이터 전체 동기화
     * - Tier, Weight, FamilyId, IsCore 업데이트
     */
    @Transactional
    public void syncTagMetadata() {
        List<Tag> allTags = tagMapper.findAllTags();
        // 캐시 업데이트
        allTags.forEach(tag -> tagNameCache.put(tag.getTagKey(), tag.getKoreanName()));

        int updatedCount = 0;

        for (Tag tag : allTags) {
            CoreTagsConfig.TagMetadata metadata = CoreTagsConfig.getMetadata(tag.getTagKey());
            boolean updated = false;

            // Update isCore
            if (!Boolean.valueOf(metadata.isCore()).equals(tag.getIsCore())) {
                tag.setIsCore(metadata.isCore());
                updated = true;
            }

            // Update isBasic
            if (!Boolean.valueOf(metadata.isBasic()).equals(tag.getIsBasic())) {
                tag.setIsBasic(metadata.isBasic());
                updated = true;
            }

            // Update Tier
            if (!metadata.tier().equals(tag.getImportanceTier())) {
                tag.setImportanceTier(metadata.tier());
                updated = true;
            }

            // Update Weight
            if (tag.getWeight() == null || Math.abs(metadata.weight() - tag.getWeight()) > 0.001) {
                tag.setWeight(metadata.weight());
                updated = true;
            }

            // Update FamilyId
            if (metadata.familyKey() != null) {
                TagFamily family = tagMapper.findFamilyByKey(metadata.familyKey());
                if (family != null && !family.getId().equals(tag.getFamilyId())) {
                    tag.setFamilyId(family.getId());
                    updated = true;
                }
            }

            if (updated) {
                tagMapper.updateTagMetadata(tag);
                updatedCount++;
            }
        }

        log.info("태그 메타데이터 동기화 완료: {}개 태그 업데이트됨", updatedCount);
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
                tagMapper.saveTagFamily(family);
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

                // CoreTagsConfig에서 메타데이터 조회
                CoreTagsConfig.TagMetadata metadata = CoreTagsConfig.getMetadata(key);

                Long familyId = null;
                if (metadata.familyKey() != null) {
                    TagFamily family = tagMapper.findFamilyByKey(metadata.familyKey());
                    if (family != null) {
                        familyId = family.getId();
                    }
                }

                Tag tag = Tag.create(
                        key,
                        bojTagId,
                        familyId,
                        null, // parentKey (현재 사용 안함)
                        metadata.tier(),
                        metadata.weight(),
                        metadata.isCore(),
                        metadata.isBasic(),
                        koreanName);

                tagMapper.saveTag(tag);
            }
        } catch (IOException e) {
            log.error("JSON 파일에서 태그 로드 실패", e);
        }
    }

    public String getKoreanName(String tagKey) {
        if (tagKey == null)
            return null;
        return tagNameCache.getOrDefault(tagKey, tagKey);
    }
}
