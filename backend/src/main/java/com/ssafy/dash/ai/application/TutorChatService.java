package com.ssafy.dash.ai.application;

import com.ssafy.dash.ai.client.AiServerClient;
import com.ssafy.dash.ai.client.dto.HintChatRequest;
import com.ssafy.dash.ai.client.dto.HintChatResponse;
import com.ssafy.dash.algorithm.domain.AlgorithmRecord;
import com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository;
import com.ssafy.dash.analytics.application.UserSkillAnalysisService;
import com.ssafy.dash.analytics.application.dto.TagWeaknessDto;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 튜터 대화 서비스
 * 
 * 맞은 문제/틀린 문제 모두에 대해 대화형 튜터링을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TutorChatService {

    private final AiServerClient aiClient;
    private final UserSkillAnalysisService skillAnalysisService;
    private final UserRepository userRepository;
    private final AlgorithmRecordRepository algorithmRecordRepository;
    private final com.ssafy.dash.acorn.application.AcornService acornService;

    /**
     * AI 튜터 대화 (recordId로 DB에서 코드/문제 정보 조회)
     * 
     * @param userId      사용자 ID
     * @param recordId    알고리즘 기록 ID
     * @param message     사용자 메시지
     * @param solveStatus 풀이 상태 (solved / wrong)
     * @param wrongReason 틀린 이유
     * @param history     대화 히스토리
     * @return AI 튜터 대화 응답
     */
    public HintChatResponse hintChatWithRecord(Long userId, Long recordId, String message,
            String solveStatus, String wrongReason, List<HintChatRequest.ChatMessage> history) {
        log.info("AI Tutor chat for record: {}, user: {}, status: {}", recordId, userId, solveStatus);

        // 1. AlgorithmRecord 조회
        AlgorithmRecord record = algorithmRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Record not found: " + recordId));

        // 2. 도토리 3개 차감
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (user.getStudyId() != null) {
            acornService.use(
                    user.getStudyId(),
                    userId,
                    3, // 대화 1회당 3개 차감
                    String.format("AI 튜터 대화 - 문제 %s", record.getProblemNumber()));
        }

        // 3. 사용자 컨텍스트 수집
        HintChatRequest.UserContext userContext = collectChatUserContext(userId);

        // 4. AI 튜터 요청 생성
        HintChatRequest request = HintChatRequest.builder()
                .message(message)
                .problemNumber(record.getProblemNumber())
                .problemTitle(record.getTitle())
                .code(record.getCode())
                .language(record.getLanguage())
                .solveStatus(solveStatus != null ? solveStatus : "wrong")
                .wrongReason(wrongReason)
                .history(history != null ? history : List.of())
                .userContext(userContext)
                .build();

        return aiClient.hintChat(request);
    }

    /**
     * 힌트 대화 (직접 요청 - deprecated, hintChatWithRecord 사용 권장)
     */
    public HintChatResponse hintChat(HintChatRequest request) {
        log.info("Hint chat for problem: {}", request.getProblemNumber());
        return aiClient.hintChat(request);
    }

    private HintChatRequest.UserContext collectChatUserContext(Long userId) {
        User user = userRepository.findById(userId).orElse(null);

        List<String> weakTags = skillAnalysisService.getWeaknessTags(userId)
                .stream()
                .limit(5)
                .map(TagWeaknessDto::getTagKey)
                .toList();

        int tier = user != null ? (user.getSolvedacTier() != null ? user.getSolvedacTier() : 0) : 0;

        return HintChatRequest.UserContext.builder()
                .tierName(getTierName(tier))
                .solvedCount(user != null && user.getSolvedCount() != null ? user.getSolvedCount() : 0)
                .weakTags(weakTags)
                .build();
    }

    private String getTierName(int tier) {
        if (tier == 0)
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
