package com.ssafy.dash.ai.application;

import com.ssafy.dash.ai.client.AiServerClient;
import com.ssafy.dash.ai.client.dto.TutorChatRequest;
import com.ssafy.dash.ai.client.dto.TutorChatResponse;
import com.ssafy.dash.analytics.domain.UserTagStat;
import com.ssafy.dash.analytics.infrastructure.persistence.UserTagStatMapper;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * 대화형 튜터 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TutorService {

    private final AiServerClient aiClient;
    private final UserRepository userRepository;
    private final UserTagStatMapper tagStatMapper;

    /**
     * 튜터와 대화
     * 
     * @param userId        사용자 ID
     * @param message       사용자 메시지
     * @param history       이전 대화 히스토리
     * @param problemNumber 관련 문제 번호 (선택)
     * @param code          관련 코드 (선택)
     * @return 튜터 응답
     */
    public TutorChatResponse chat(Long userId, String message,
            List<TutorChatRequest.ChatMessage> history,
            String problemNumber, String code) {
        log.info("Tutor chat for user: {}", userId);

        // 사용자 컨텍스트 수집
        TutorChatRequest.UserContext userContext = collectUserContext(userId);

        // 대화 요청 생성
        TutorChatRequest request = TutorChatRequest.builder()
                .message(message)
                .problemNumber(problemNumber)
                .code(code)
                .history(history != null ? history : List.of())
                .context(userContext)
                .build();

        return aiClient.chat(request);
    }

    private TutorChatRequest.UserContext collectUserContext(Long userId) {
        User user = userRepository.findById(userId).orElse(null);

        // 최근 태그 (가장 많이 푼 태그)
        List<String> recentTags = tagStatMapper.findByUserId(userId).stream()
                .sorted(Comparator.comparing(UserTagStat::getSolved).reversed())
                .limit(5)
                .map(UserTagStat::getTagKey)
                .toList();

        String tier = user != null ? getTierName(user.getSolvedacTier()) : "Unrated";
        int solvedCount = user != null && user.getSolvedacRating() != null ? user.getSolvedacRating() : 0;

        return TutorChatRequest.UserContext.builder()
                .tier(tier)
                .solvedCount(solvedCount)
                .recentTags(recentTags)
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
