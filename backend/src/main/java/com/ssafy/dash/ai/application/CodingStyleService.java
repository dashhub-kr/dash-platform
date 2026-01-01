package com.ssafy.dash.ai.application;

import com.ssafy.dash.ai.infrastructure.client.AiServerClient;
import com.ssafy.dash.ai.infrastructure.client.dto.request.CodingStyleRequest;
import com.ssafy.dash.ai.infrastructure.client.dto.response.CodingStyleResponse;
import com.ssafy.dash.algorithm.domain.AlgorithmRecord;
import com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository;
import com.ssafy.dash.analytics.domain.UserTagStat;
import com.ssafy.dash.analytics.domain.UserTagStatRepository;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * 코딩 스타일 분석 서비스 (MBTI 스타일)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodingStyleService {

        private final AiServerClient aiClient;
        private final AlgorithmRecordRepository recordRepository;
        private final UserTagStatRepository tagStatRepository;
        private final UserRepository userRepository;

        /**
         * 사용자의 코딩 스타일 분석
         * 
         * @param userId 사용자 ID
         * @return MBTI 스타일 분석 결과
         */
        public CodingStyleResponse analyzeCodingStyle(Long userId) {
                log.info("Analyzing coding style for user: {}", userId);

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

                // 최근 코드 샘플 수집 (최대 10개)
                List<AlgorithmRecord> recentRecords = recordRepository.findByUserId(userId);
                List<CodingStyleRequest.CodeSample> codeSamples = recentRecords.stream()
                                .filter(r -> r.getCode() != null && !r.getCode().isEmpty())
                                .limit(10)
                                .map(this::toCodeSample)
                                .toList();

                if (codeSamples.isEmpty()) {
                        log.info("No code samples found for user {}. Returning default coding style.", userId);
                        return CodingStyleResponse.builder()
                                        .mbtiCode("READY")
                                        .nickname("준비된 도전자")
                                        .summary("아직 분석할 코드가 없습니다. 문제를 풀고 커밋하면 AI가 코딩 스타일을 분석해드립니다.")
                                        .strengths(java.util.List.of("열정", "잠재력"))
                                        .improvements(java.util.List.of())
                                        .compatibleStyles("")
                                        .build();
                }

                // 사용자 통계 수집
                CodingStyleRequest.UserStats userStats = collectUserStats(user, recentRecords);

                CodingStyleRequest request = CodingStyleRequest.builder()
                                .codeSamples(codeSamples)
                                .userStats(userStats)
                                .build();

                return aiClient.analyzeCodingStyle(request);
        }

        private CodingStyleRequest.CodeSample toCodeSample(AlgorithmRecord record) {
                return CodingStyleRequest.CodeSample.builder()
                                .code(record.getCode())
                                .language(record.getLanguage() != null ? record.getLanguage() : "java")
                                .problemNumber(record.getProblemNumber())
                                .runtimeMs(record.getRuntimeMs() != null ? record.getRuntimeMs() : 0)
                                .memoryKb(record.getMemoryKb() != null ? record.getMemoryKb() : 0)
                                .build();
        }

        private CodingStyleRequest.UserStats collectUserStats(User user, List<AlgorithmRecord> records) {
                // 평균 런타임/메모리 계산
                double avgRuntime = records.stream()
                                .filter(r -> r.getRuntimeMs() != null)
                                .mapToInt(AlgorithmRecord::getRuntimeMs)
                                .average()
                                .orElse(0);

                double avgMemory = records.stream()
                                .filter(r -> r.getMemoryKb() != null)
                                .mapToInt(AlgorithmRecord::getMemoryKb)
                                .average()
                                .orElse(0);

                // 선호 태그 (가장 많이 푼 태그)
                List<String> preferredTags = tagStatRepository.findByUserId(user.getId()).stream()
                                .sorted(Comparator.comparing(UserTagStat::getSolved).reversed())
                                .limit(5)
                                .map(UserTagStat::getTagKey)
                                .toList();

                // 티어 이름
                String tier = getTierName(user.getSolvedacTier());

                return CodingStyleRequest.UserStats.builder()
                                .totalSolved(user.getSolvedacRating() != null ? user.getSolvedacRating()
                                                : records.size())
                                .avgRuntime(avgRuntime)
                                .avgMemory(avgMemory)
                                .preferredTags(preferredTags)
                                .tier(tier)
                                .build();
        }

        private String getTierName(Integer tier) {
                if (tier == null || tier == 0)
                        return "Unrated";
                String[] tiers = { "Bronze", "Silver", "Gold", "Platinum", "Diamond", "Ruby" };
                String[] levels = { "V", "IV", "III", "II", "I" };
                int tierIndex = (tier - 1) / 5;
                int levelIndex = (tier - 1) % 5;
                if (tierIndex >= tiers.length)
                        return "Master";
                return tiers[tierIndex] + " " + levels[levelIndex];
        }
}
