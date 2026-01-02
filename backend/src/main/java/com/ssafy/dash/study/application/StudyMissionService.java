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
import com.ssafy.dash.study.domain.StudyMission.MissionStatus;
import com.ssafy.dash.study.domain.StudyMissionRepository;
import com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository;
import com.ssafy.dash.study.domain.StudyMissionSubmission;
import com.ssafy.dash.study.domain.StudyMissionSubmissionRepository;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.study.application.dto.result.MissionWithProgressResult;
import com.ssafy.dash.study.application.dto.result.MemberProgressResult;

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

    /**
     * 미션 생성 (직접 입력)
     */
    public StudyMission createMissionManual(Long studyId, Integer week, String title,
            List<Integer> problemIds, LocalDate deadline) {
        if (deadline != null && deadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("마감일은 과거 날짜일 수 없습니다.");
        }
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
        if (deadline != null && deadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("마감일은 과거 날짜일 수 없습니다.");
        }
        String problemsJson = toJson(problemIds);
        StudyMission mission = StudyMission.create(studyId, week, title, problemsJson, "AI_RECOMMENDED", deadline);
        missionRepository.save(mission);

        initializeSubmissions(mission.getId(), studyId, problemIds);

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
    }

    /**
     * 스터디의 미션 목록 조회
     */
    /**
     * 스터디의 미션 목록 조회
     */
    /**
     * 스터디의 미션 목록 조회 (algorithm_records와 자동 동기화)
     */
    public List<MissionWithProgressResult> getMissions(Long studyId, Long requestUserId) {
        List<StudyMission> missions = missionRepository.findByStudyIdOrderByWeekDesc(studyId);
        List<User> members = userRepository.findByStudyId(studyId);
        List<MissionWithProgressResult> result = new ArrayList<>();

        for (StudyMission mission : missions) {
            List<Integer> problemIds = parseProblems(mission.getProblemIds());
            int totalProblems = problemIds.size();

            // 조회 시점에 algorithm_records와 동기화 (누락된 완료 상태 업데이트)
            syncSubmissionsWithRecords(mission.getId(), members, problemIds);

            // Calculate progress for requesting user (legacy field support if needed)
            int solvedCount = submissionRepository.countCompletedByMissionIdAndUserId(mission.getId(), requestUserId);

            // Calculate progress for ALL members
            List<MemberProgressResult> memberProgressList = new ArrayList<>();
            for (User member : members) {
                int memberCompleted = submissionRepository.countCompletedByMissionIdAndUserId(mission.getId(),
                        member.getId());
                List<Integer> solvedProblemIds = submissionRepository.findCompletedProblemIds(mission.getId(),
                        member.getId());
                List<Integer> sosProblemIds = submissionRepository.findSosProblemIds(mission.getId(), member.getId());

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

            // 미션 완료 여부 판단 (DB 상태 우선, 그 외엔 전원 완료 or 데드라인)
            // 전원 완료 체크 로직은 기존 유지
            // Note: 모든 멤버 완료 여부는 memberProgressList의 allCompleted 필드로 FE에서 확인 가능

            // Status가 null이면 IN_PROGRESS로 취급
            MissionStatus status = mission.getStatus();
            if (status == null)
                status = MissionStatus.IN_PROGRESS;

            // 자동 완료 조건: 모든 멤버가 다 풀었거나(Optional), 데드라인이 지났으면서 모두 풀었나?
            // 일단 DB status를 그대로 반환하고, FE에서 처리하거나, 여기서 자동 완료 처리 로직을 넣을 수도 있음.
            // 요구사항: "모든 팀원이 다 풀었거나 or 스터디장이 강제 완료 했거나"
            // 여기서는 보여주기용 Status를 계산해서 DTO에 담을지, DB값을 그대로 줄지 결정.
            // -> DB 값을 그대로 주고, 계산된 '완료 여부'도 같이 주자.

            // 만약 DB상으론 진행중인데 모든 멤버가 다 풀었다면? -> 자동으로 완료 상태로 간주 (시스템적 완료)
            // 하지만 DB 업데이트는 하지 않고 API 응답에서만 처리할 수도 있고, checkAndMarkCompleted 등에서 업데이트 해줄 수도
            // 있음.
            // 여기서는 일단 DB 상태값을 그대로 반환. (FE에서 status == COMPLETED || allMembersCompleted 로직
            // 처리)

            result.add(new MissionWithProgressResult(
                    mission.getId(),
                    mission.getWeek(),
                    mission.getTitle(),
                    problemIds,
                    mission.getSourceType(),
                    mission.getDeadline(),
                    mission.getStatus(),
                    solvedCount,
                    totalProblems,
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

                // Check if already solved
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

}
