package com.ssafy.dash.study.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dash.analytics.domain.UserFamilyStat;
import com.ssafy.dash.analytics.domain.UserTagStatRepository;
import com.ssafy.dash.problem.application.ProblemService;
import com.ssafy.dash.problem.domain.ProblemRecommendationResponse;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 스터디 팀 분석 서비스
 * - 멤버별 패밀리 통계 조회 (레이더차트용)
 * - 팀 평균 분석
 * - 커리큘럼 생성
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyAnalysisService {

    private final UserRepository userRepository;
    private final UserTagStatRepository userTagStatRepository;
    private final ProblemService problemService;

    /**
     * 팀 패밀리 통계 (레이더차트용) - 절대값(실제 푼 문제 수) 합산
     */
    public List<TeamFamilyStat> getTeamFamilyStats(Long studyId) {
        List<User> members = userRepository.findByStudyId(studyId);
        if (members.isEmpty()) {
            return List.of();
        }

        // 패밀리별 합산 통계
        Map<String, TeamFamilyStat> familyMap = new HashMap<>();

        for (User member : members) {
            List<UserFamilyStat> memberFamilyStats = userTagStatRepository.findFamilyStatsByUserId(member.getId());

            for (UserFamilyStat stat : memberFamilyStats) {
                String key = stat.getFamilyKey();
                TeamFamilyStat teamStat = familyMap.computeIfAbsent(key,
                        k -> new TeamFamilyStat(k, stat.getFamilyName()));
                teamStat.addStats(stat.getSolved());
            }
        }

        return new ArrayList<>(familyMap.values());
    }

    /**
     * 팀 추천 태그 - 정답률 낮은 패밀리의 하위 태그 중 추천
     */
    public List<RecommendedTag> getTeamRecommendedTags(Long studyId) {
        List<TeamFamilyStat> familyStats = getTeamFamilyStats(studyId);
        if (familyStats.isEmpty()) {
            return List.of();
        }

        // 정답률 낮은 순으로 정렬
        familyStats.sort((a, b) -> Double.compare(a.getCompletionRate(), b.getCompletionRate()));

        // 상위 3개 약점 패밀리의 태그를 추천
        List<RecommendedTag> recommended = new ArrayList<>();
        for (int i = 0; i < Math.min(3, familyStats.size()); i++) {
            TeamFamilyStat weak = familyStats.get(i);
            recommended.add(new RecommendedTag(
                    weak.getFamilyKey().toLowerCase(),
                    weak.getFamilyName(),
                    weak.getCompletionRate()));
        }

        return recommended;
    }

    /**
     * 커리큘럼 문제 추천 - 약점 태그 기반 문제 조회
     */
    public List<ProblemRecommendationResponse> getCurriculumProblems(Long studyId) {
        List<User> members = userRepository.findByStudyId(studyId);
        if (members.isEmpty()) {
            return List.of();
        }

        // 팀 평균 티어 계산
        int avgTier = (int) Math.round(members.stream()
                .filter(m -> m.getSolvedacTier() != null)
                .mapToInt(User::getSolvedacTier)
                .average()
                .orElse(1.0));

        // 추천 태그 가져오기
        List<RecommendedTag> recommended = getTeamRecommendedTags(studyId);
        if (recommended.isEmpty()) {
            return List.of();
        }

        // 각 추천 태그별로 문제 2개씩 가져오기
        List<ProblemRecommendationResponse> curriculum = new ArrayList<>();
        for (RecommendedTag tag : recommended) {
            List<ProblemRecommendationResponse> problems = problemService.getRecommendedProblems(tag.tagKey(), avgTier,
                    null);
            curriculum.addAll(problems.subList(0, Math.min(2, problems.size())));
        }

        return curriculum;
    }

    /**
     * 기존 분석 메서드 (하위 호환)
     */
    public StudyAnalysisResult analyzeStudy(Long studyId) {
        List<User> members = userRepository.findByStudyId(studyId);
        if (members.isEmpty()) {
            return StudyAnalysisResult.empty();
        }

        // 패밀리 기반 통계 사용
        List<TeamFamilyStat> familyStats = getTeamFamilyStats(studyId);
        Map<String, Double> teamAverages = new HashMap<>();
        List<WeaknessTag> weaknessTags = new ArrayList<>();

        for (TeamFamilyStat stat : familyStats) {
            double rate = stat.getCompletionRate();
            teamAverages.put(stat.getFamilyKey().toLowerCase(), rate);
            weaknessTags.add(new WeaknessTag(stat.getFamilyKey().toLowerCase(), rate));
        }

        // 약점 태그 정렬 (낮은 순)
        weaknessTags.sort((a, b) -> Double.compare(a.averageRate(), b.averageRate()));

        // 평균 티어 계산
        double avgTier = members.stream()
                .filter(m -> m.getSolvedacTier() != null)
                .mapToInt(User::getSolvedacTier)
                .average()
                .orElse(0.0);

        // 멤버별 패밀리 통계
        List<MemberTagStats> memberStats = new ArrayList<>();
        for (User member : members) {
            List<UserFamilyStat> memberFamilyStats = userTagStatRepository.findFamilyStatsByUserId(member.getId());
            Map<String, Double> tagRates = new HashMap<>();
            Map<String, Integer> tagSolved = new HashMap<>();
            for (UserFamilyStat stat : memberFamilyStats) {
                String key = stat.getFamilyKey().toLowerCase();
                tagRates.put(key, stat.getCompletionRate());
                tagSolved.put(key, stat.getSolved());
            }
            memberStats.add(new MemberTagStats(
                    member.getId(),
                    member.getUsername(),
                    member.getSolvedacTier(),
                    tagRates,
                    tagSolved));
        }

        return new StudyAnalysisResult(
                memberStats,
                teamAverages,
                avgTier,
                weaknessTags.subList(0, Math.min(3, weaknessTags.size())));
    }

    // DTO Records
    public record StudyAnalysisResult(
            List<MemberTagStats> memberStats,
            Map<String, Double> teamAverages,
            double averageTier,
            List<WeaknessTag> topWeaknesses) {
        public static StudyAnalysisResult empty() {
            return new StudyAnalysisResult(List.of(), Map.of(), 0.0, List.of());
        }
    }

    public record MemberTagStats(
            Long userId,
            String username,
            Integer tier,
            Map<String, Double> tagRates,
            Map<String, Integer> tagSolved) {
    }

    public record WeaknessTag(
            String tagKey,
            double averageRate) {
    }

    public record RecommendedTag(
            String tagKey,
            String tagName,
            double completionRate) {
    }

    public static class TeamFamilyStat {
        private final String familyKey;
        private final String familyName;
        private int solved = 0;

        public TeamFamilyStat(String familyKey, String familyName) {
            this.familyKey = familyKey;
            this.familyName = familyName;
        }

        public void addStats(Integer addSolved) {
            this.solved += (addSolved != null ? addSolved : 0);
        }

        public String getFamilyKey() {
            return familyKey;
        }

        public String getFamilyName() {
            return familyName;
        }

        public int getSolved() {
            return solved;
        }

        public int getTotal() {
            return solved; // AlgorithmRadarChart에서 상대적 비교 용도
        }

        public double getCompletionRate() {
            return solved; // 절대값 반환
        }
    }
}
