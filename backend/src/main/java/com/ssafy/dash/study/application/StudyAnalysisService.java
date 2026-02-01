package com.ssafy.dash.study.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.ssafy.dash.problem.presentation.dto.response.ProblemRecommendationResponse;
import com.ssafy.dash.problem.domain.Tag;
import com.ssafy.dash.problem.domain.CoreTagsConfig;
import com.ssafy.dash.problem.infrastructure.persistence.TagMapper;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.study.application.dto.result.StudyAnalysisResult;
import com.ssafy.dash.study.application.dto.result.MemberTagStatsResult;
import com.ssafy.dash.study.application.dto.result.WeaknessTagResult;
import com.ssafy.dash.study.application.dto.result.TeamFamilyStatResult;

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
    public List<TeamFamilyStatResult> getTeamFamilyStats(Long studyId) {
        List<User> members = userRepository.findByStudyId(studyId);
        if (members.isEmpty()) {
            return List.of();
        }

        // 패밀리별 합산 통계
        Map<String, TeamFamilyStatResult> familyMap = new HashMap<>();

        for (User member : members) {
            List<UserFamilyStat> memberFamilyStats = userTagStatRepository.findFamilyStatsByUserId(member.getId());

            for (UserFamilyStat stat : memberFamilyStats) {
                String key = stat.getFamilyKey();
                TeamFamilyStatResult teamStat = familyMap.computeIfAbsent(key,
                        k -> new TeamFamilyStatResult(k, stat.getFamilyName()));
                teamStat.addStats(stat.getSolved());
            }
        }

        return new ArrayList<>(familyMap.values());
    }

    /**
     * 팀 추천 태그 - 정답률 낮은 패밀리의 하위 태그 중 추천
     */
    public List<RecommendedTag> getTeamRecommendedTags(Long studyId) {
        List<TeamFamilyStatResult> familyStats = getTeamFamilyStats(studyId);
        if (familyStats.isEmpty()) {
            return List.of();
        }

        // 정답률 낮은 순으로 정렬
        familyStats.sort((a, b) -> Double.compare(a.getCompletionRate(), b.getCompletionRate()));

        // 상위 3개 약점 패밀리의 태그를 추천
        List<RecommendedTag> recommended = new ArrayList<>();
        for (int i = 0; i < Math.min(3, familyStats.size()); i++) {
            TeamFamilyStatResult weak = familyStats.get(i);
            recommended.add(new RecommendedTag(
                    weak.getFamilyKey().toLowerCase(),
                    weak.getFamilyName(),
                    weak.getCompletionRate()));
        }

        return recommended;
    }

    /**
     * 커리큘럼 문제 추천 (v5) - 점수 기반 통합 랭킹 + 엄격한 필터링
     * 
     * 변경 사항:
     * - 태그별 고정 할당량(2개) 폐지 -> 전체 문제 풀에서 점수순 상위 18개 선정
     * - 필터링 강화: 최대 2명이 푼 문제까지만 추천 (3명 이상 풀면 제외)
     * - 점수 요소: 태그 중요도 + 난이도 적합성 + 미풀이 보너스
     */
    public List<ProblemRecommendationResponse> getCurriculumProblems(Long studyId) {
        List<User> members = userRepository.findByStudyId(studyId);
        if (members.isEmpty()) {
            return List.of();
        }

        // 1. 팀 최저 티어 계산
        int minTier = members.stream()
                .filter(m -> m.getSolvedacTier() != null)
                .mapToInt(User::getSolvedacTier)
                .min()
                .orElse(1);

        // 2. 후보 태그 선정 (코테 빈출 태그 중 점수 상위)
        List<Tag> allTags = tagMapper.findAllTags().stream()
                .filter(tag -> TIER_WEIGHTS.containsKey(tag.getImportanceTier()))
                .filter(tag -> CoreTagsConfig.isCoreTag(tag.getTagKey()))
                .toList();

        if (allTags.isEmpty()) {
            return List.of();
        }

        List<ScoredTag> scoredTags = new ArrayList<>();
        for (Tag tag : allTags) {
            double score = calculateTagScore(tag, members);
            if (score > 0) {
                scoredTags.add(new ScoredTag(tag.getTagKey(), tag.getDisplayNames(), score));
            }
        }

        // 상위 10개 태그 선정
        scoredTags.sort(Comparator.comparing(ScoredTag::score).reversed());
        List<ScoredTag> candidateTags = scoredTags.stream().limit(10).toList();

        // 3. 제외할 문제 ID 수집 (현재 미션에 포함된 문제 - 중복 추천 방지)
        List<java.util.Set<String>> memberSolvedSets = members.stream()
                .map(m -> algorithmRecordRepository.findSolvedProblemNumbers(m.getId()))
                .toList();

        Set<String> excludeProblemIds = new HashSet<>();
        List<StudyMission> missions = studyMissionRepository.findByStudyId(studyId);
        for (StudyMission mission : missions) {
            try {
                List<Integer> ids = objectMapper.readValue(mission.getProblemIds(), new TypeReference<List<Integer>>() {
                });
                excludeProblemIds.addAll(ids.stream().map(String::valueOf).toList());
            } catch (Exception e) {
                // ignore
            }
        }

        // 4. 모든 후보 문제 수집 및 점수화
        List<ScoredProblem> candidateProblems = new ArrayList<>();
        int teamSize = members.size();

        for (ScoredTag tag : candidateTags) {
            // 태그별로 넉넉히 10개씩 조회
            List<ProblemRecommendationResponse> problems = problemService.getRecommendedProblems(
                    tag.tagKey(), minTier, null, new ArrayList<>(excludeProblemIds));

            for (ProblemRecommendationResponse p : problems) {
                // 중복 체크
                if (excludeProblemIds.contains(p.getProblemId()))
                    continue;

                // 푼 멤버 확인
                List<ProblemRecommendationResponse.SolvedMember> solvedBy = new ArrayList<>();
                for (int i = 0; i < members.size(); i++) {
                    if (memberSolvedSets.get(i).contains(p.getProblemId())) {
                        User m = members.get(i);
                        solvedBy.add(new ProblemRecommendationResponse.SolvedMember(m.getId(), m.getUsername(),
                                m.getAvatarUrl()));
                    }
                }

                int solvedCount = solvedBy.size();

                // [필터링 핵심]
                // 1. 전원 풀이 제외 (solvedCount < teamSize)
                // 2. 최대 2명까지만 푼 문제 허용 (solvedCount <= 2)
                if (solvedCount < teamSize && solvedCount <= 2) {

                    // 점수 계산
                    double problemScore = tag.score();

                    // 난이도 적합도 (최저 티어와 가까울수록 점수 높음)
                    double tierDiff = Math.abs(p.getLevel() - minTier);
                    problemScore += (10.0 / (tierDiff + 1));

                    // 미풀이 보너스 (아무도 안 풀었으면 가산점)
                    if (solvedCount == 0) {
                        problemScore += 5.0;
                    }

                    // Response 객체 업데이트
                    ProblemRecommendationResponse updatedResponse = new ProblemRecommendationResponse(
                            p.getProblemId(), p.getTitle(), p.getLevel(), p.getTags(), solvedBy);

                    candidateProblems.add(new ScoredProblem(updatedResponse, problemScore));
                    excludeProblemIds.add(p.getProblemId()); // 중복 방지용 추가
                }
            }
        }

        // 5. 점수 높은 순으로 정렬 후 상위 18개 반환
        return candidateProblems.stream()
                .sorted(Comparator.comparing(ScoredProblem::score).reversed())
                .limit(18)
                .map(ScoredProblem::problem)
                .toList();
    }

    // 내부 헬퍼 레코드
    record ScoredProblem(ProblemRecommendationResponse problem, double score) {
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
        List<TeamFamilyStatResult> familyStats = getTeamFamilyStats(studyId);
        Map<String, Double> teamAverages = new HashMap<>();
        List<WeaknessTagResult> weaknessTags = new ArrayList<>();

        for (TeamFamilyStatResult stat : familyStats) {
            double rate = stat.getCompletionRate();
            teamAverages.put(stat.getFamilyKey().toLowerCase(), rate);
            weaknessTags.add(new WeaknessTagResult(stat.getFamilyKey().toLowerCase(), rate));
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
        List<MemberTagStatsResult> memberStats = new ArrayList<>();
        for (User member : members) {
            List<UserFamilyStat> memberFamilyStats = userTagStatRepository.findFamilyStatsByUserId(member.getId());
            Map<String, Double> tagRates = new HashMap<>();
            Map<String, Integer> tagSolved = new HashMap<>();
            for (UserFamilyStat stat : memberFamilyStats) {
                String key = stat.getFamilyKey().toLowerCase();
                tagRates.put(key, stat.getCompletionRate());
                tagSolved.put(key, stat.getSolved());
            }
            memberStats.add(new MemberTagStatsResult(
                    member.getId(),
                    member.getUsername(),
                    member.getSolvedacTier(),
                    member.getAvatarUrl(),
                    member.getEquippedDecorationClass(),
                    tagRates,
                    tagSolved));
        }

        return new StudyAnalysisResult(
                memberStats,
                teamAverages,
                avgTier,
                weaknessTags.subList(0, Math.min(3, weaknessTags.size())));
    }

    // 내부 헬퍼 레코드 (프레젠테이션 계층에 노출되지 않음)
    record RecommendedTag(
            String tagKey,
            String tagName,
            double completionRate) {
    }

    record ScoredTag(
            String tagKey,
            String displayName,
            double score) {
    }
}
