package com.ssafy.dash.study.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.ssafy.dash.study.domain.StudyMission;
import com.ssafy.dash.study.domain.StudyMissionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dash.analytics.domain.UserFamilyStat;
import com.ssafy.dash.analytics.domain.UserTagStat;
import com.ssafy.dash.analytics.domain.UserTagStatRepository;
import com.ssafy.dash.analytics.infrastructure.persistence.UserTagStatMapper;
import com.ssafy.dash.problem.application.ProblemService;
import com.ssafy.dash.problem.domain.ProblemRecommendationResponse;
import com.ssafy.dash.problem.domain.Tag;
import com.ssafy.dash.problem.infrastructure.persistence.TagMapper;
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
    private final UserTagStatMapper userTagStatMapper;
    private final TagMapper tagMapper;
    private final ProblemService problemService;
    private final com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository algorithmRecordRepository;
    private final StudyMissionRepository studyMissionRepository;
    private final ObjectMapper objectMapper;

    // 티어별 가중치 (S=3, A=2, B=1)
    private static final Map<String, Double> TIER_WEIGHTS = Map.of(
            "S", 3.0,
            "A", 2.0,
            "B", 1.0);

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
     * 커리큘럼 문제 추천 (v2) - 세부 태그 기반 점수화 시스템 + 최저 티어 기준
     * 
     * 점수화 로직:
     * - S티어 = 3.0, A티어 = 2.0, B티어 = 1.0 (C티어 제외)
     * - isCore = true면 ×1.5 추가
     * - 팀 평균 풀이수 <= 5: ×2.0, <= 15: ×1.5
     * - 1명 이상 학습중(1~9문제): ×1.2 보너스
     */
    public List<ProblemRecommendationResponse> getCurriculumProblems(Long studyId) {
        List<User> members = userRepository.findByStudyId(studyId);
        if (members.isEmpty()) {
            return List.of();
        }

        // 팀 최저 티어 계산 (모든 팀원이 풀 수 있는 난이도)
        int minTier = members.stream()
                .filter(m -> m.getSolvedacTier() != null)
                .mapToInt(User::getSolvedacTier)
                .min()
                .orElse(1);

        // 모든 세부 태그 조회 (S, A, B 티어만)
        List<Tag> allTags = tagMapper.findAllTags().stream()
                .filter(tag -> TIER_WEIGHTS.containsKey(tag.getImportanceTier()))
                .toList();

        if (allTags.isEmpty()) {
            return List.of();
        }

        // 태그별 점수 계산
        List<ScoredTag> scoredTags = new ArrayList<>();
        for (Tag tag : allTags) {
            double score = calculateTagScore(tag, members);
            if (score > 0) {
                scoredTags.add(new ScoredTag(tag.getTagKey(), tag.getDisplayNames(), score));
            }
        }

        // 점수 높은 순으로 정렬 후 상위 10개 후보 선택 (넉넉하게)
        scoredTags.sort(Comparator.comparing(ScoredTag::score).reversed());
        List<ScoredTag> candidateTags = scoredTags.stream().limit(10).toList();

        // 각 태그별로 문제 2개씩 추천 (모든 멤버가 푼 문제는 제외)
        List<java.util.Set<String>> memberSolvedSets = members.stream()
                .map(m -> algorithmRecordRepository.findSolvedProblemNumbers(m.getId()))
                .toList();

        // 3. 진행 중인 미션의 문제들도 제외 목록에 추가
        List<String> missionProblemIds = new ArrayList<>();
        List<StudyMission> missions = studyMissionRepository.findByStudyId(studyId);
        for (StudyMission mission : missions) {
            try {
                List<Integer> ids = objectMapper.readValue(mission.getProblemIds(), new TypeReference<List<Integer>>() {
                });
                missionProblemIds.addAll(ids.stream().map(String::valueOf).toList());
            } catch (Exception e) {
                // 파싱 에러 무시
            }
        }

        List<ProblemRecommendationResponse> curriculum = new ArrayList<>();
        int selectedTagCount = 0;

        for (ScoredTag tag : candidateTags) {
            // 목표: 태그 5개 선정
            if (selectedTagCount >= 5)
                break;

            // 필터링을 위해 넉넉히 20개 조회 (Mapper LIMIT 20)
            // excludeIds에 미션 문제 목록 전달
            List<ProblemRecommendationResponse> problems = problemService.getRecommendedProblems(
                    tag.tagKey(), minTier, null, missionProblemIds);

            List<ProblemRecommendationResponse> newProblems = new ArrayList<>();

            for (ProblemRecommendationResponse p : problems) {
                if (newProblems.size() >= 2)
                    break;

                String pNum = p.getProblemId();
                boolean allSolved = memberSolvedSets.stream().allMatch(set -> set.contains(pNum));

                if (!allSolved) {
                    newProblems.add(p);
                }
            }

            // 안 푼 문제가 2개 이상 확보된 경우에만 이 태그를 커리큘럼에 포함
            if (newProblems.size() >= 2) {
                curriculum.addAll(newProblems);
                selectedTagCount++;
            }
        }

        return curriculum;
    }

    /**
     * 태그 점수 계산
     */
    private double calculateTagScore(Tag tag, List<User> members) {
        // 기본 티어 가중치
        double tierWeight = TIER_WEIGHTS.getOrDefault(tag.getImportanceTier(), 0.0);
        if (tierWeight == 0)
            return 0;

        // isCore 보너스
        if (Boolean.TRUE.equals(tag.getIsCore())) {
            tierWeight *= 1.5;
        }

        // 멤버별 풀이 수 수집
        List<Integer> solvedCounts = new ArrayList<>();
        int learningCount = 0; // 1~9문제 푼 멤버 수

        for (User member : members) {
            UserTagStat stat = userTagStatMapper.findByUserIdAndTag(member.getId(), tag.getTagKey());
            int solved = (stat != null && stat.getSolved() != null) ? stat.getSolved() : 0;
            solvedCounts.add(solved);
            if (solved >= 1 && solved < 10) {
                learningCount++;
            }
        }

        // 팀 평균 풀이 수
        double avgSolved = solvedCounts.stream().mapToInt(i -> i).average().orElse(0);

        // 풀이 수 기반 가중치 (적게 풀수록 높은 점수)
        double solvedWeight = 1.0;
        if (avgSolved <= 5) {
            solvedWeight = 2.0;
        } else if (avgSolved <= 15) {
            solvedWeight = 1.5;
        } else if (avgSolved <= 30) {
            solvedWeight = 1.0;
        } else {
            solvedWeight = 0.5; // 많이 푼 태그는 낮은 점수
        }

        // 학습중 보너스
        double learningBonus = learningCount > 0 ? 1.2 : 1.0;

        return tierWeight * solvedWeight * learningBonus;
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

    public record ScoredTag(
            String tagKey,
            String displayName,
            double score) {
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
