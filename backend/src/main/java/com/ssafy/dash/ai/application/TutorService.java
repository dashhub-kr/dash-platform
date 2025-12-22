package com.ssafy.dash.ai.application;

import com.ssafy.dash.ai.client.AiServerClient;
import com.ssafy.dash.ai.client.dto.TutorChatRequest;
import com.ssafy.dash.ai.client.dto.TutorChatResponse;
import com.ssafy.dash.ai.domain.TutorConversation;
import com.ssafy.dash.ai.infrastructure.TutorConversationMapper;
import com.ssafy.dash.analytics.domain.UserTagStat;
import com.ssafy.dash.analytics.infrastructure.persistence.UserTagStatMapper;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * 대화형 튜터 서비스
 * 
 * DB 기반 대화 저장 및 세션 관리 지원
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TutorService {

    private static final int MAX_HISTORY_SIZE = 10; // AI에 전송할 최대 히스토리
    private static final int SESSION_TIMEOUT_MINUTES = 60; // 세션 타임아웃

    private final AiServerClient aiClient;
    private final UserRepository userRepository;
    private final UserTagStatMapper tagStatMapper;
    private final TutorConversationMapper conversationMapper;

    /**
     * 튜터와 대화 (세션 기반)
     * 
     * @param userId        사용자 ID
     * @param sessionId     세션 ID (null이면 새 세션 생성)
     * @param message       사용자 메시지
     * @param problemNumber 관련 문제 번호 (선택)
     * @param code          관련 코드 (선택)
     * @return 세션 ID가 포함된 튜터 응답
     */
    @Transactional
    public TutorSessionResponse chat(Long userId, String sessionId, String message,
            String problemNumber, String code) {
        log.info("Tutor chat for user: {}, session: {}", userId, sessionId);

        // 1. 세션 관리
        sessionId = resolveSessionId(userId, sessionId);

        // 2. 기존 대화 조회 (최근 10개)
        List<TutorConversation> conversations = conversationMapper.findRecentBySession(
                userId, sessionId, MAX_HISTORY_SIZE);

        // 3. AI 요청용 히스토리 변환
        List<TutorChatRequest.ChatMessage> history = conversations.stream()
                .map(c -> TutorChatRequest.ChatMessage.builder()
                        .role(c.getRole())
                        .content(c.getContent())
                        .build())
                .toList();

        // 4. AI 서버 호출
        TutorChatRequest.UserContext userContext = collectUserContext(userId);
        TutorChatRequest request = TutorChatRequest.builder()
                .message(message)
                .problemNumber(problemNumber)
                .code(code)
                .history(history)
                .context(userContext)
                .build();

        TutorChatResponse aiResponse = aiClient.chat(request);

        // 5. 대화 저장
        conversationMapper.insert(TutorConversation.userMessage(userId, sessionId, message, problemNumber));
        conversationMapper.insert(TutorConversation.assistantMessage(userId, sessionId, aiResponse.getReply()));

        // 6. 응답 반환
        return TutorSessionResponse.builder()
                .sessionId(sessionId)
                .reply(aiResponse.getReply())
                .teachingStyle(aiResponse.getTeachingStyle())
                .followUpQuestions(aiResponse.getFollowUpQuestions())
                .conceptExplanation(aiResponse.getConceptExplanation())
                .encouragement(aiResponse.getEncouragement())
                .build();
    }

    /**
     * 레거시 메서드 (히스토리 직접 전달 방식)
     * 
     * @deprecated 세션 기반 chat() 메서드 사용 권장
     */
    @Deprecated
    public TutorChatResponse chat(Long userId, String message,
            List<TutorChatRequest.ChatMessage> history,
            String problemNumber, String code) {
        log.info("Tutor chat (legacy) for user: {}", userId);

        TutorChatRequest.UserContext userContext = collectUserContext(userId);

        TutorChatRequest request = TutorChatRequest.builder()
                .message(message)
                .problemNumber(problemNumber)
                .code(code)
                .history(history != null ? history : List.of())
                .context(userContext)
                .build();

        return aiClient.chat(request);
    }

    /**
     * 세션 ID 확인 및 생성
     * 
     * - sessionId가 null이면 새 세션 생성
     * - 기존 세션이 타임아웃되었으면 새 세션 생성
     */
    private String resolveSessionId(Long userId, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return UUID.randomUUID().toString();
        }

        // 세션 타임아웃 확인
        LocalDateTime lastActivity = conversationMapper.findLastActivityBySession(userId, sessionId);
        if (lastActivity != null) {
            Duration elapsed = Duration.between(lastActivity, LocalDateTime.now());
            if (elapsed.toMinutes() > SESSION_TIMEOUT_MINUTES) {
                log.info("Session timed out, creating new session. Old: {}", sessionId);
                return UUID.randomUUID().toString();
            }
        }

        return sessionId;
    }

    private TutorChatRequest.UserContext collectUserContext(Long userId) {
        User user = userRepository.findById(userId).orElse(null);

        List<String> recentTags = tagStatMapper.findByUserId(userId).stream()
                .sorted(Comparator.comparing(UserTagStat::getSolved).reversed())
                .limit(5)
                .map(UserTagStat::getTagKey)
                .toList();

        String tier = user != null ? getTierName(user.getSolvedacTier()) : "Unrated";
        int solvedCount = user != null && user.getSolvedCount() != null ? user.getSolvedCount() : 0;

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

    // === 응답 DTO ===

    @lombok.Builder
    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class TutorSessionResponse {
        private String sessionId;
        private String reply;
        private String teachingStyle;
        private List<String> followUpQuestions;
        private String conceptExplanation;
        private String encouragement;
    }
}
