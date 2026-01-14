package com.ssafy.dash.social.application.dto.result;

import java.time.LocalDateTime;

/**
 * 친구 활동 피드 아이템
 */
public record FeedResult(
                Long id,
                String type, // SOLVED, STREAK, BATTLE, CHALLENGE_RECEIVED

                // 유저 정보
                Long userId,
                String userName,
                String userAvatar,
                String userDecorationClass,
                Integer userTier,

                // SOLVED 타입
                Long problemId,
                String problemTitle,

                // STREAK 타입
                Integer streakDays,

                // BATTLE 타입
                Long battleId,
                String battleType, // MOCK_EXAM, DEFENSE
                Boolean isWinner,
                Boolean isDraw,
                Integer problemCount,

                LocalDateTime createdAt) {
        // SOLVED 피드 생성
        public static FeedResult solved(Long id, Long userId, String userName, String userAvatar,
                        String userDecorationClass, Integer userTier,
                        Long problemId, String problemTitle, LocalDateTime createdAt) {
                return new FeedResult(id, "SOLVED", userId, userName, userAvatar, userDecorationClass, userTier,
                                problemId, problemTitle, null, null, null, null, null, null, createdAt);
        }

        // STREAK 피드 생성
        public static FeedResult streak(Long id, Long userId, String userName, String userAvatar,
                        String userDecorationClass, Integer userTier,
                        Integer streakDays, LocalDateTime createdAt) {
                return new FeedResult(id, "STREAK", userId, userName, userAvatar, userDecorationClass, userTier,
                                null, null, streakDays, null, null, null, null, null, createdAt);
        }

        // BATTLE 피드 생성
        public static FeedResult battle(Long id, Long userId, String userName, String userAvatar,
                        String userDecorationClass, Integer userTier,
                        Long battleId, String battleType, Boolean isWinner, Boolean isDraw, LocalDateTime createdAt) {
                return new FeedResult(id, "BATTLE", userId, userName, userAvatar, userDecorationClass, userTier,
                                null, null, null, battleId, battleType, isWinner, isDraw, null, createdAt);
        }

        // CHALLENGE_RECEIVED 피드 생성
        public static FeedResult challengeReceived(Long id, Long userId, String userName, String userAvatar,
                        String userDecorationClass,
                        Integer userTier,
                        Long battleId, String battleType, Integer problemCount, LocalDateTime createdAt) {
                return new FeedResult(id, "CHALLENGE_RECEIVED", userId, userName, userAvatar, userDecorationClass,
                                userTier,
                                null, null, null, battleId, battleType, null, null, problemCount, createdAt);
        }
}
