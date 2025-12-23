package com.ssafy.dash.study.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.study.domain.StudyMission;
import com.ssafy.dash.study.domain.StudyMissionRepository;
import com.ssafy.dash.study.domain.StudyMissionSubmission;
import com.ssafy.dash.study.domain.StudyMissionSubmissionRepository;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 스터디 주차별 미션 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class StudyMissionService {

    private final StudyMissionRepository missionRepository;
    private final StudyMissionSubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * 미션 생성 (직접 입력)
     */
    public StudyMission createMissionManual(Long studyId, Integer week, String title,
            List<Integer> problemIds, LocalDate deadline) {
        String problemsJson = toJson(problemIds);
        StudyMission mission = StudyMission.create(studyId, week, title, problemsJson, "MANUAL", deadline);
        missionRepository.save(mission);

        // 스터디 멤버들의 submission 레코드 초기화
        initializeSubmissions(mission.getId(), studyId, problemIds);

        return mission;
    }

    /**
     * 미션 생성 (AI 추천 문제 사용)
     */
    public StudyMission createMissionFromCurriculum(Long studyId, Integer week, String title,
            List<Integer> problemIds, LocalDate deadline) {
        String problemsJson = toJson(problemIds);
        StudyMission mission = StudyMission.create(studyId, week, title, problemsJson, "AI_RECOMMENDED", deadline);
        missionRepository.save(mission);

        initializeSubmissions(mission.getId(), studyId, problemIds);

        return mission;
    }

    /**
     * 스터디의 미션 목록 조회
     */
    /**
     * 스터디의 미션 목록 조회
     */
    @Transactional(readOnly = true)
    public List<MissionWithProgress> getMissions(Long studyId, Long requestUserId) {
        List<StudyMission> missions = missionRepository.findByStudyIdOrderByWeekDesc(studyId);
        List<User> members = userRepository.findByStudyId(studyId);
        List<MissionWithProgress> result = new ArrayList<>();

        for (StudyMission mission : missions) {
            List<Integer> problemIds = parseProblems(mission.getProblemIds());
            int totalProblems = problemIds.size();

            // Calculate progress for requesting user (legacy field support if needed)
            int solvedCount = submissionRepository.countCompletedByMissionIdAndUserId(mission.getId(), requestUserId);

            // Calculate progress for ALL members
            List<MemberProgress> memberProgressList = new ArrayList<>();
            for (User member : members) {
                int memberCompleted = submissionRepository.countCompletedByMissionIdAndUserId(mission.getId(),
                        member.getId());
                memberProgressList.add(new MemberProgress(
                        member.getId(),
                        member.getUsername(),
                        memberCompleted,
                        totalProblems,
                        totalProblems > 0 && memberCompleted == totalProblems));
            }

            result.add(new MissionWithProgress(
                    mission.getId(),
                    mission.getWeek(),
                    mission.getTitle(),
                    problemIds,
                    mission.getSourceType(),
                    mission.getDeadline(),
                    solvedCount,
                    totalProblems,
                    memberProgressList));
        }

        return result;
    }

    /**
     * 미션별 멤버 진행 현황 조회
     */
    @Transactional(readOnly = true)
    public List<MemberProgress> getMissionProgress(Long missionId) {
        StudyMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("미션을 찾을 수 없습니다."));

        List<Integer> problemIds = parseProblems(mission.getProblemIds());
        int totalProblems = problemIds.size();

        List<User> members = userRepository.findByStudyId(mission.getStudyId());
        List<MemberProgress> result = new ArrayList<>();

        for (User member : members) {
            int completed = submissionRepository.countCompletedByMissionIdAndUserId(missionId, member.getId());
            result.add(new MemberProgress(
                    member.getId(),
                    member.getUsername(),
                    completed,
                    totalProblems,
                    totalProblems > 0 && completed == totalProblems));
        }

        return result;
    }

    /**
     * 문제 완료 체크 (GitHub webhook에서 호출)
     */
    public void checkAndMarkCompleted(Long userId, Integer problemId) {
        // 사용자가 속한 스터디의 미션 중 해당 문제가 포함된 미션 찾기
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getStudyId() == null)
            return;

        List<StudyMission> missions = missionRepository.findByStudyId(user.getStudyId());
        for (StudyMission mission : missions) {
            List<Integer> problems = parseProblems(mission.getProblemIds());
            if (problems.contains(problemId)) {
                // Submission 레코드가 존재하는지 확인하고, 없으면 생성
                StudyMissionSubmission submission = submissionRepository
                        .findByMissionIdAndUserIdAndProblemId(mission.getId(), userId, problemId);

                if (submission == null) {
                    submission = StudyMissionSubmission.create(mission.getId(), userId, problemId);
                    submissionRepository.save(submission);
                }

                // 완료 처리
                submissionRepository.markCompleted(mission.getId(), userId, problemId);
            }
        }
    }

    private void initializeSubmissions(Long missionId, Long studyId, List<Integer> problemIds) {
        List<User> members = userRepository.findByStudyId(studyId);
        for (User member : members) {
            for (Integer problemId : problemIds) {
                StudyMissionSubmission submission = StudyMissionSubmission.create(missionId, member.getId(), problemId);
                submissionRepository.save(submission);
            }
        }
    }

    /**
     * 특정 문제가 현재 진행 중인 미션에 포함되는지 확인
     */
    @Transactional(readOnly = true)
    public boolean isActiveMissionProblem(Long studyId, Integer problemId, java.time.LocalDateTime now) {
        List<StudyMission> missions = missionRepository.findByStudyId(studyId);
        for (StudyMission mission : missions) {
            // Check deadline (assuming mission is active if before deadline or within same
            // week? Logic: Deadline is date.)
            // Logic: If now is before deadline + 1 day (end of deadline day).
            if (mission.getDeadline() != null && now.toLocalDate().isAfter(mission.getDeadline())) {
                continue;
            }

            List<Integer> problems = parseProblems(mission.getProblemIds());
            if (problems.contains(problemId)) {
                return true;
            }
        }
        return false;
    }

    private String toJson(List<Integer> problems) {
        try {
            return objectMapper.writeValueAsString(problems);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("문제 목록 직렬화 실패", e);
        }
    }

    private List<Integer> parseProblems(String json) {
        if (json == null || json.isBlank())
            return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Integer>>() {
            });
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    // DTOs
    public record MissionWithProgress(
            Long id,
            Integer week,
            String title,
            List<Integer> problemIds,
            String sourceType,
            LocalDate deadline,
            int solvedCount,
            int totalProblems,
            List<MemberProgress> memberProgressList) {
    }

    public record MemberProgress(
            Long userId,
            String username,
            int completedCount,
            int totalProblems,
            boolean allCompleted) {
    }
}
