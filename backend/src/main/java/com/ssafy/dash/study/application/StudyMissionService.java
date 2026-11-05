package com.ssafy.dash.study.application;

import java.time.LocalDate;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.common.exception.BusinessException;
import com.ssafy.dash.common.exception.ErrorCode;
import com.ssafy.dash.study.domain.StudyMission;
import com.ssafy.dash.study.domain.StudyMission.MissionStatus;
import com.ssafy.dash.study.domain.StudyMissionRepository;
import com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository;
import com.ssafy.dash.study.domain.StudyMissionSubmission;
import com.ssafy.dash.study.domain.StudyMissionSubmissionRepository;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.study.application.dto.result.MissionWithProgressResult;
import com.ssafy.dash.study.application.dto.result.MemberProgressResult;
import com.ssafy.dash.study.application.dto.result.MissionProblemInfo;
import com.ssafy.dash.notification.application.NotificationService;
import com.ssafy.dash.notification.domain.NotificationType;
import com.ssafy.dash.problem.infrastructure.persistence.ProblemMapper;
import com.ssafy.dash.problem.application.TagService;
import com.ssafy.dash.problem.domain.Problem;

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
    private final AlgorithmRecordRepository algorithmRecordRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final ProblemMapper problemMapper;
    private final TagService tagService;

    /**
     * 미션이 해당 스터디에 속하는지 확인한다 (다른 스터디 미션 조작 방지).
     */
    @Transactional(readOnly = true)
    public void requireMissionInStudy(Long missionId, Long studyId) {
        StudyMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        if (!Objects.equals(mission.getStudyId(), studyId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    /**
     * 미션 생성 (직접 입력)
     */
    public StudyMission createMissionManual(Long studyId, Integer week, String title,
            List<Integer> problemIds, LocalDate deadline) {
        if (deadline != null && deadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("마감일은 과거 날짜일 수 없습니다.");
        }

        // Check if mission for the week already exists
        Optional<StudyMission> existingMissionOpt = missionRepository.findByStudyIdAndWeek(studyId, week);
        if (existingMissionOpt.isPresent()) {
            StudyMission existingMission = existingMissionOpt.get();
            // Merge problems
            List<Integer> currentProblems = parseProblems(existingMission.getProblemIds());
            List<Integer> mergedProblems = new ArrayList<>(currentProblems);
            for (Integer pid : problemIds) {
                if (!mergedProblems.contains(pid)) {
                    mergedProblems.add(pid);
                }
            }
            // Update metadata and problem list
            return updateMission(existingMission.getId(), title, deadline, mergedProblems);
        }

        String problemsJson = toJson(problemIds);
        StudyMission mission = StudyMission.create(studyId, week, title, problemsJson, "MANUAL", deadline);
        missionRepository.save(mission);

        // 스터디 멤버들의 submission 레코드 초기화
        initializeSubmissions(mission.getId(), studyId, problemIds);

        // [Notification] New Mission
        List<User> members = userRepository.findByStudyId(studyId);
        for (User member : members) {
            notificationService.send(member.getId(), "새로운 미션이 등록되었습니다: " + title, "/study/missions",
                    NotificationType.MISSION);
        }

        return mission;
    }

    /**
     * 미션 생성 (AI 추천 문제 사용)
     */
    public StudyMission createMissionFromCurriculum(Long studyId, Integer week, String title,
            List<Integer> problemIds, LocalDate deadline) {
        if (deadline != null && deadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("마감일은 과거 날짜일 수 없습니다.");
        }

        Optional<StudyMission> existingMissionOpt = missionRepository.findByStudyIdAndWeek(studyId, week);
        if (existingMissionOpt.isPresent()) {
            StudyMission existingMission = existingMissionOpt.get();
            List<Integer> currentProblems = parseProblems(existingMission.getProblemIds());
            List<Integer> mergedProblems = new ArrayList<>(currentProblems);
            for (Integer pid : problemIds) {
                if (!mergedProblems.contains(pid)) {
                    mergedProblems.add(pid);
                }
            }
            return updateMission(existingMission.getId(), title, deadline, mergedProblems);
        }

        String problemsJson = toJson(problemIds);
        StudyMission mission = StudyMission.create(studyId, week, title, problemsJson, "AI_RECOMMENDED", deadline);
        missionRepository.save(mission);

        initializeSubmissions(mission.getId(), studyId, problemIds);

        // [Notification] New Mission
        List<User> members = userRepository.findByStudyId(studyId);
        for (User member : members) {
            notificationService.send(member.getId(), "AI가 새로운 미션을 추천했습니다: " + title, "/study/missions",
                    NotificationType.MISSION);
        }

        return mission;
    }

    /**
     * 기존 미션에 문제 추가
     */
    public void addProblemsToMission(Long missionId, List<Integer> newProblemIds) {
        StudyMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("미션을 찾을 수 없습니다."));

        List<Integer> currentProblems = parseProblems(mission.getProblemIds());
        // ArrayList로 변경하여 수정 가능하게 함
        List<Integer> updatedProblems = new ArrayList<>(currentProblems);
        List<Integer> addedProblems = new ArrayList<>();

        for (Integer pid : newProblemIds) {
            if (!updatedProblems.contains(pid)) {
                updatedProblems.add(pid);
                addedProblems.add(pid);
            }
        }

        if (addedProblems.isEmpty()) {
            return;
        }

        mission.setProblemIds(toJson(updatedProblems));
        missionRepository.update(mission);

        // 새로 추가된 문제들에 대해서만 submission 초기화
        initializeSubmissions(mission.getId(), mission.getStudyId(), addedProblems);
    }

    /**
     * 미션에서 문제 제거
     */
    public void removeProblemFromMission(Long missionId, Integer problemId) {
        StudyMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("미션을 찾을 수 없습니다."));

        List<Integer> currentProblems = parseProblems(mission.getProblemIds());
        List<Integer> updatedProblems = new ArrayList<>(currentProblems);

        if (updatedProblems.remove(problemId)) {
            mission.setProblemIds(toJson(updatedProblems));
            missionRepository.update(mission);

            // 해당 문제에 대한 submission 데이터 삭제
            submissionRepository.deleteByMissionIdAndProblemId(missionId, problemId);
        }
    }

    /**
     * 미션 정보 수정 (제목, 마감일, 문제 목록)
     */
    public StudyMission updateMission(Long missionId, String title, LocalDate deadline, List<Integer> problemIds) {
        StudyMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("미션을 찾을 수 없습니다."));

        if (title != null && !title.isBlank()) {
            mission.setTitle(title);
        }
        if (deadline != null) {
            if (deadline.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("마감일은 과거 날짜일 수 없습니다.");
            }
            mission.setDeadline(deadline);
        }

        if (problemIds != null) {
            List<Integer> currentProblems = parseProblems(mission.getProblemIds());

            // 1. Identify removed problems
            List<Integer> removedProblems = new ArrayList<>(currentProblems);
            removedProblems.removeAll(problemIds);

            // 2. Identify added problems
            List<Integer> addedProblems = new ArrayList<>(problemIds);
            addedProblems.removeAll(currentProblems);

            // 3. Update mission with NEW complete list (preserves order)
            mission.setProblemIds(toJson(problemIds));

            // 4. Process cleanup for removed problems
            for (Integer pid : removedProblems) {
                submissionRepository.deleteByMissionIdAndProblemId(missionId, pid);
            }

            // 5. Initialize submissions for added problems
            if (!addedProblems.isEmpty()) {
                initializeSubmissions(mission.getId(), mission.getStudyId(), addedProblems);
            }
        }

        missionRepository.update(mission);
        return mission;
    }

    /**
     * 미션 강제 완료 (Status 변경)
     */
    public void completeMission(Long missionId) {
        StudyMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("미션을 찾을 수 없습니다."));

        mission.setStatus(MissionStatus.COMPLETED);
        missionRepository.update(mission);
    }

    /**
     * 미션 삭제
     */
    public void deleteMission(Long missionId) {
        // 1. 관련 Submissions 삭제
        submissionRepository.deleteByMissionId(missionId);

        // 2. 미션 삭제
        missionRepository.delete(missionId);
    }

    /**
     * SOS 상태 토글
     */
    public void toggleSos(Long missionId, Integer problemId, Long userId) {
        // 이미 푼 문제인지 확인
        StudyMissionSubmission submission = submissionRepository
                .findByMissionIdAndUserIdAndProblemId(missionId, userId, problemId);

        if (submission == null) {
            submission = StudyMissionSubmission.create(missionId, userId, problemId);
            submissionRepository.save(submission);
        }

        if (Boolean.TRUE.equals(submission.getCompleted())) {
            throw new IllegalStateException("이미 해결한 문제에는 SOS를 요청할 수 없습니다.");
        }

        boolean newStatus = !Boolean.TRUE.equals(submission.getIsSos());
        submissionRepository.updateSosStatus(missionId, userId, problemId, newStatus);

        // [Notification] SOS Request
        if (newStatus) { // Only when SOS is turned ON
            User requester = userRepository.findById(userId).orElseThrow();
            StudyMission mission = missionRepository.findById(missionId).orElseThrow();
            List<User> members = userRepository.findByStudyId(mission.getStudyId());

            for (User member : members) {
                if (!member.getId().equals(userId)) { // Don't notify self
                    notificationService.send(
                            member.getId(),
                            String.format("%s님이 %d주차 %d번 문제에 SOS를 요청했습니다!", requester.getUsername(), mission.getWeek(),
                                    problemId),
                            "/study/missions",
                            NotificationType.SOS);
                }
            }
        }
    }

    /**
     * 스터디의 미션 목록 조회 (algorithm_records와 자동 동기화)
     * 수정: 동기화 후 최신 데이터 조회하여 정합성 보장
     */
    public List<MissionWithProgressResult> getMissions(Long studyId, Long requestUserId) {
        List<StudyMission> missions = missionRepository.findByStudyIdOrderByWeekDesc(studyId);
        if (missions.isEmpty()) {
            return new ArrayList<>();
        }

        List<User> members = userRepository.findByStudyId(studyId);
        List<MissionWithProgressResult> result = new ArrayList<>();

        for (StudyMission mission : missions) {
            List<Integer> problemIds = parseProblems(mission.getProblemIds());
            int totalProblems = problemIds.size();

            // 문제 상세 정보 조회 (N+1 쿼리 방지: 배치 조회)
            List<MissionProblemInfo> problems = new ArrayList<>();
            if (!problemIds.isEmpty()) {
                List<String> problemNumberStrs = problemIds.stream().map(String::valueOf).toList();
                List<Problem> problemEntities = problemMapper.findProblemsByNumbers(problemNumberStrs);
                
                // [FIX] 한 번에 모든 태그 조회 후 Map으로 그룹화
                List<com.ssafy.dash.problem.domain.ProblemTag> allTags = problemMapper.findTagsByProblemNumbers(problemNumberStrs);
                java.util.Map<String, List<String>> tagsByProblem = allTags.stream()
                        .collect(java.util.stream.Collectors.groupingBy(
                                com.ssafy.dash.problem.domain.ProblemTag::getProblemNumber,
                                java.util.stream.Collectors.mapping(
                                        com.ssafy.dash.problem.domain.ProblemTag::getTagKey,
                                        java.util.stream.Collectors.toList())));
                
                for (Problem p : problemEntities) {
                    List<String> tags = tagsByProblem.getOrDefault(p.getProblemNumber(), List.of())
                            .stream()
                            .map(tagService::getKoreanName)
                            .toList();
                    problems.add(new MissionProblemInfo(p.getProblemNumber(), p.getTitle(), p.getLevel(), tags));
                }
            }

            // [FIX] 1. 먼저 동기화 수행 (algorithm_records -> submissions DB 업데이트)
            syncSubmissionsWithRecords(mission.getId(), members, problemIds);

            // [FIX] 2. 동기화 후 최신 데이터 조회
            List<StudyMissionSubmission> missionSubmissionList = submissionRepository.findByMissionId(mission.getId());
            java.util.Map<Long, List<StudyMissionSubmission>> missionSubmissions = missionSubmissionList.stream()
                    .collect(java.util.stream.Collectors.groupingBy(StudyMissionSubmission::getUserId));

            List<StudyMissionSubmission> mySubmissions = missionSubmissions.getOrDefault(requestUserId,
                    new ArrayList<>());
            int solvedCount = (int) mySubmissions.stream().filter(s -> Boolean.TRUE.equals(s.getCompleted())).count();

            // 현재 사용자가 해결한 문제 ID 목록
            List<Integer> mySolvedProblemIds = mySubmissions.stream()
                    .filter(s -> Boolean.TRUE.equals(s.getCompleted()))
                    .map(StudyMissionSubmission::getProblemId)
                    .collect(java.util.stream.Collectors.toList());

            // 모든 멤버의 진행률 계산
            List<MemberProgressResult> memberProgressList = new ArrayList<>();
            for (User member : members) {

                List<StudyMissionSubmission> userSubmissions = missionSubmissions.getOrDefault(member.getId(),
                        new ArrayList<>());

                int memberCompleted = (int) userSubmissions.stream().filter(s -> Boolean.TRUE.equals(s.getCompleted()))
                        .count();

                List<Integer> solvedProblemIds = userSubmissions.stream()
                        .filter(s -> Boolean.TRUE.equals(s.getCompleted()))
                        .map(StudyMissionSubmission::getProblemId)
                        .collect(java.util.stream.Collectors.toList());

                List<Integer> sosProblemIds = userSubmissions.stream()
                        .filter(s -> Boolean.TRUE.equals(s.getIsSos()))
                        .map(StudyMissionSubmission::getProblemId)
                        .collect(java.util.stream.Collectors.toList());

                memberProgressList.add(new MemberProgressResult(
                        member.getId(),
                        member.getUsername(),
                        member.getAvatarUrl(),
                        memberCompleted,
                        totalProblems,
                        totalProblems > 0 && memberCompleted == totalProblems,
                        solvedProblemIds,
                        sosProblemIds));
            }

            MissionStatus status = mission.getStatus();
            if (status == null)
                status = MissionStatus.IN_PROGRESS;

            result.add(new MissionWithProgressResult(
                    mission.getId(),
                    mission.getWeek(),
                    mission.getTitle(),
                    problemIds,
                    problems, // NEW: 문제 상세 정보
                    mission.getSourceType(),
                    mission.getDeadline(),
                    mission.getStatus(),
                    solvedCount,
                    totalProblems,
                    mySolvedProblemIds, // NEW: 현재 사용자가 해결한 문제 ID 목록
                    memberProgressList));
        }

        return result;
    }


    /**
     * algorithm_records와 study_mission_submissions 동기화
     * - Dashboard와 동일한 데이터 소스 기반으로 통일
     */
    private void syncSubmissionsWithRecords(Long missionId, List<User> members, List<Integer> problemIds) {
        for (User member : members) {
            for (Integer problemId : problemIds) {
                // algorithm_records에 성공 기록이 있는지 확인
                boolean hasSolvedRecord = algorithmRecordRepository.existsSuccessfulSubmission(
                        member.getId(), String.valueOf(problemId));

                if (hasSolvedRecord) {
                    // submission 레코드 조회
                    StudyMissionSubmission submission = submissionRepository
                            .findByMissionIdAndUserIdAndProblemId(missionId, member.getId(), problemId);

                    if (submission == null) {
                        // submission이 없으면 생성하고 완료 처리
                        submission = StudyMissionSubmission.create(missionId, member.getId(), problemId);
                        submission.markCompleted();
                        submissionRepository.save(submission);
                    } else if (!Boolean.TRUE.equals(submission.getCompleted())) {
                        // submission이 있지만 완료 처리 안 되어있으면 완료 처리
                        submissionRepository.markCompleted(missionId, member.getId(), problemId);
                    }
                }
            }
        }
    }

    /**
     * 미션별 멤버 진행 현황 조회
     */
    @Transactional(readOnly = true)
    public List<MemberProgressResult> getMissionProgress(Long missionId) {
        StudyMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("미션을 찾을 수 없습니다."));

        List<Integer> problemIds = parseProblems(mission.getProblemIds());
        int totalProblems = problemIds.size();

        List<User> members = userRepository.findByStudyId(mission.getStudyId());
        List<MemberProgressResult> result = new ArrayList<>();

        for (User member : members) {
            int completed = submissionRepository.countCompletedByMissionIdAndUserId(missionId, member.getId());
            List<Integer> solvedProblemIds = submissionRepository.findCompletedProblemIds(missionId, member.getId());
            List<Integer> sosProblemIds = submissionRepository.findSosProblemIds(missionId, member.getId());

            result.add(new MemberProgressResult(
                    member.getId(),
                    member.getUsername(),
                    member.getAvatarUrl(),
                    completed,
                    totalProblems,
                    totalProblems > 0 && completed == totalProblems,
                    solvedProblemIds,
                    sosProblemIds));
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

                // [Notification] Problem Solved
                List<User> members = userRepository.findByStudyId(user.getStudyId());
                for (User member : members) {
                    if (!member.getId().equals(userId)) {
                        notificationService.send(
                                member.getId(),
                                String.format("%s님이 %d번 문제를 해결했습니다.", user.getUsername(), problemId),
                                "/study/missions",
                                NotificationType.SOLVED);
                    }
                }

                // [Notification] Mission Completed (All members solved all problems)
                checkAndNotifyMissionCompletion(mission, members);
            }
        }
    }

    private void checkAndNotifyMissionCompletion(StudyMission mission, List<User> members) {
        List<Integer> problemIds = parseProblems(mission.getProblemIds());
        int totalProblems = problemIds.size();
        if (totalProblems == 0)
            return;

        boolean allMembersCompleted = true;
        for (User member : members) {
            int completedCount = submissionRepository.countCompletedByMissionIdAndUserId(mission.getId(),
                    member.getId());
            if (completedCount < totalProblems) {
                allMembersCompleted = false;
                break;
            }
        }

        if (allMembersCompleted) {

            if (mission.getStatus() != MissionStatus.COMPLETED) {
                // Update status
                mission.setStatus(MissionStatus.COMPLETED);
                missionRepository.update(mission);

                // 전체 알림 전송
                for (User member : members) {
                    notificationService.send(
                            member.getId(),
                            String.format("축하합니다! %d주차 미션을 팀 전원이 완수했습니다! 🎉", mission.getWeek()),
                            "/study/missions",
                            NotificationType.MISSION_COMPLETED);
                }
            }
        }
    }

    private void initializeSubmissions(Long missionId, Long studyId, List<Integer> problemIds) {
        List<User> members = userRepository.findByStudyId(studyId);
        for (User member : members) {
            for (Integer problemId : problemIds) {
                // 이미 존재하는지 확인
                if (submissionRepository.findByMissionIdAndUserIdAndProblemId(missionId, member.getId(),
                        problemId) != null) {
                    continue;
                }

                StudyMissionSubmission submission = StudyMissionSubmission.create(missionId, member.getId(), problemId);

                // 이미 해결했는지 확인
                if (algorithmRecordRepository.existsSuccessfulSubmission(member.getId(), String.valueOf(problemId))) {
                    submission.markCompleted();
                }

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
            // 마감일 확인 (마감일 이전이거나 같은 주 내라면 활성 상태로 가정? 로직: 마감일은 날짜임.)
            // 로직: 현재 시각이 마감일 + 1일(마감일의 끝) 이전인 경우.
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

}
