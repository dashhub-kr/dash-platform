package com.ssafy.dash.study.application;

import com.ssafy.dash.algorithm.application.AlgorithmRecordService;
import com.ssafy.dash.algorithm.domain.StudyStats;
import com.ssafy.dash.study.application.dto.result.StudyStatsResult;
import com.ssafy.dash.study.domain.Study;
import com.ssafy.dash.study.domain.StudyRepository;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.user.application.dto.result.UserResult;
import com.ssafy.dash.user.presentation.dto.response.UserResponse;
import com.ssafy.dash.notification.application.NotificationService;
import com.ssafy.dash.notification.domain.NotificationType;
import com.ssafy.dash.study.domain.Study.StudyType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dash.study.domain.StudyApplication;
import com.ssafy.dash.study.domain.StudyVisibility;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final UserRepository userRepository;
    private final AlgorithmRecordService algorithmRecordService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<Study> findAll() {
        return studyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Study> searchByKeyword(String keyword) {
        return studyRepository.searchByKeyword(keyword);
    }

    @Transactional
    public Study createStudy(Long userId, String name, String description, StudyVisibility visibility) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getStudyId() != null) {
            Study currentStudy = studyRepository.findById(user.getStudyId()).orElse(null);
            // PERSONAL 타입이 아니면 중복 가입 불가 (비정상 상태 방지)
            if (currentStudy != null && currentStudy.getStudyType() == StudyType.GROUP) {
                throw new IllegalStateException("User already belongs to a study");
            }
        }

        // 대기 중인 가입 신청이 있으면 자동 취소
        studyRepository.findPendingApplicationByUserId(userId).ifPresent(app -> {
            studyRepository.deleteApplication(app.getId());
        });

        Study study = Study.create(name, userId);
        study.setDescription(description);
        study.setVisibility(visibility != null ? visibility : StudyVisibility.PUBLIC);
        study.setStudyType(StudyType.GROUP); // 기본은 GROUP

        studyRepository.save(study);

        // 변경 전 유저 정보를 업데이트하기 전에 기존 스터디 참조를 저장합니다.
        Long oldStudyId = user.getStudyId();
        Study oldStudy = oldStudyId != null ? studyRepository.findById(oldStudyId).orElse(null) : null;

        // 스터디장은 자동으로 가입 처리됩니다. (FK 제약 조건을 피하기 위해 유저 정보 먼저 업데이트)
        user.updateStudy(study.getId());
        userRepository.update(user);

        // 이제 기존 PERSONAL 스터디는 안전하게 삭제할 수 있습니다. (유저가 더 이상 참조하지 않으므로)
        if (oldStudy != null && oldStudy.getStudyType() == StudyType.PERSONAL) {
            algorithmRecordService.migrateStudyId(oldStudy.getId(), study.getId());
            studyRepository.delete(oldStudy.getId());
        }

        return study;
    }

    @Transactional
    public Study createPersonalStudy(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getStudyId() != null) {
            return studyRepository.findById(user.getStudyId()).orElse(null);
        }

        Study study = Study.create(user.getUsername() + "의 연구실", userId);
        study.setStudyType(StudyType.PERSONAL);
        study.setVisibility(StudyVisibility.PRIVATE);
        study.setDescription("개인 학습을 위한 공간입니다.");

        studyRepository.save(study);

        user.updateStudy(study.getId());
        userRepository.update(user);
        return study;
    }

    @Transactional
    public void applyForStudy(Long userId, Long studyId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getStudyId() != null) {
            Study currentStudy = studyRepository.findById(user.getStudyId()).orElse(null);
            // PERSONAL 타입이면 지원 허용
            if (currentStudy != null && currentStudy.getStudyType() == StudyType.GROUP) {
                throw new IllegalStateException("User already belongs to a study");
            }
        }

        // 스터디 존재 여부 확인
        if (studyRepository.findById(studyId).isEmpty()) {
            throw new IllegalArgumentException("Study not found");
        }

        // 이미 가입 신청했는지 확인
        studyRepository.findApplicationByStudyIdAndUserId(studyId, userId).ifPresent(app -> {
            if (app.getStatus() == StudyApplication.ApplicationStatus.PENDING) {
                throw new IllegalStateException("Already applied to this study");
            }
            // PENDING 상태가 아니라면 (예: 승인되었다가 탈퇴한 경우), 이전 신청 기록 삭제
            studyRepository.deleteApplication(app.getId());
        });

        StudyApplication application = StudyApplication.create(studyId, userId, message);
        studyRepository.saveApplication(application);

        // 스터디장에게 알림 전송
        Study study = studyRepository.findById(studyId).orElseThrow();
        notificationService.send(
                study.getCreatorId(),
                String.format("%s님이 스터디 가입을 신청했습니다.", user.getUsername()),
                "/study/missions",
                NotificationType.STUDY_REQUEST,
                application.getId());
    }

    @Transactional(readOnly = true)
    public List<StudyApplication> getPendingApplications(Long userId, Long studyId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("Study not found"));

        if (!Objects.equals(study.getCreatorId(), userId)) {
            throw new SecurityException("Only creator can view applications");
        }

        return studyRepository.findPendingApplicationsByStudyId(studyId);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<StudyApplication> getMyPendingApplication(Long userId) {
        return studyRepository.findPendingApplicationByUserId(userId);
    }

    @Transactional
    public void cancelApplication(Long userId, Long applicationId) {
        StudyApplication application = studyRepository.findApplicationById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (!Objects.equals(application.getUserId(), userId)) {
            throw new SecurityException("Cannot cancel other's application");
        }

        application.reject();
        studyRepository.updateApplicationStatus(application);
    }

    @Transactional
    public void approveApplication(Long adminId, Long applicationId) {
        StudyApplication application = studyRepository.findApplicationById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        Study study = studyRepository.findById(application.getStudyId())
                .orElseThrow(() -> new IllegalArgumentException("Study not found"));

        if (!Objects.equals(study.getCreatorId(), adminId)) {
            throw new SecurityException("Only creator can approve applications");
        }

        // 유저를 스터디에 추가
        User user = userRepository.findById(application.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 유효성 검사: 유저가 이미 다른 GROUP 스터디에 소속되어 있는지 확인
        if (user.getStudyId() != null) {
            Study currentStudy = studyRepository.findById(user.getStudyId()).orElse(null);
            if (currentStudy != null && currentStudy.getStudyType() == StudyType.GROUP) {
                // 이미 GROUP 스터디에 소속된 경우 - 신청 자동 거절
                studyRepository.deleteApplication(applicationId);

                notificationService.send(
                        user.getId(),
                        String.format("'%s' 스터디 가입 신청이 자동 거절되었습니다.\n(이미 다른 스터디에 소속되어 있음)", study.getName()),
                        "/",
                        NotificationType.STUDY_RESULT);

                throw new IllegalStateException("신청자가 이미 다른 스터디에 소속되어 있어 승인할 수 없습니다.");
            }

            // PERSONAL 스터디인 경우 - 기록 마이그레이션
            if (currentStudy != null && currentStudy.getStudyType() == StudyType.PERSONAL) {
                algorithmRecordService.migrateStudyId(currentStudy.getId(), study.getId());
                // studyRepository.delete(currentStudy.getId()); // 유저 업데이트 이후로 이동됨
            }
        }

        application.approve();
        studyRepository.updateApplicationStatus(application);

        Long oldStudyId = user.getStudyId();
        user.updateStudy(study.getId());
        userRepository.update(user);

        // 기존 Personal 스터디가 있다면 삭제
        if (oldStudyId != null) {
             Study oldStudy = studyRepository.findById(oldStudyId).orElse(null);
             if (oldStudy != null && oldStudy.getStudyType() == StudyType.PERSONAL) {
                 studyRepository.delete(oldStudyId);
             }
        }

        // 신청자에게 알림 전송
        notificationService.send(
                user.getId(),
                String.format("'%s' 스터디 가입 신청이 승인되었습니다.", study.getName()),
                "/study/missions",
                NotificationType.STUDY_RESULT);
    }

    @Transactional
    public void rejectApplication(Long leaderId, Long applicationId, String reason) {
        StudyApplication application = studyRepository.findApplicationById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        Study study = studyRepository.findById(application.getStudyId())
                .orElseThrow(() -> new IllegalArgumentException("Study not found"));

        if (!Objects.equals(study.getCreatorId(), leaderId)) {
            throw new SecurityException("Only creator can reject applications");
        }

        // DB에서 삭제하여 재가입 가능하도록 함
        studyRepository.deleteApplication(applicationId);

        String message = String.format("'%s' 스터디 가입 신청이 거절되었습니다.", study.getName());
        if (reason != null && !reason.isBlank()) {
            message += "\n사유: " + reason;
        }

        // Notify Applicant
        notificationService.send(
                application.getUserId(),
                message,
                "/",
                NotificationType.STUDY_RESULT);
    }

    @Transactional
    public void leaveStudy(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getStudyId() == null) {
            throw new IllegalStateException("User is not in a study");
        }

        Long oldStudyId = user.getStudyId();
        Study study = studyRepository.findById(oldStudyId)
                .orElseThrow(() -> new IllegalArgumentException("Study not found"));

        if (Objects.equals(study.getCreatorId(), userId)) {
            // 정책: 스터디장은 탈퇴할 수 없음. 스터디를 해체하거나 권한을 위임해야 함. (아직 구현되지 않음)
            throw new IllegalStateException("스터디장은 탈퇴할 수 없습니다. 스터디를 해체하거나 권한을 위임해야 합니다.");
        }

        // 1. Group 스터디 탈퇴
        user.updateStudy(null);
        userRepository.update(user);

        // 2. Personal 스터디 생성 (Fallback)
        Study personalStudy = createPersonalStudy(userId);

        // 3. 유저의 기록을 Group에서 Personal 스터디로 마이그레이션
        algorithmRecordService.migrateUserRecords(userId, oldStudyId, personalStudy.getId());
    }

    @Transactional(readOnly = true)
    public StudyStatsResult getStudyStats(Long studyId) {
        StudyStats stats = algorithmRecordService.getStudyStats(studyId);
        return new StudyStatsResult(stats.bronze(), stats.silver(), stats.gold(), stats.platinum());
    }

    @Transactional(readOnly = true)
    public java.util.Optional<Study> findStudyById(Long studyId) {
        return studyRepository.findById(studyId);
    }

    @Transactional
    public void deleteStudy(Long userId, Long studyId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("Study not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 스터디장 또는 관리자만 삭제 가능
        boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
        if (!Objects.equals(study.getCreatorId(), userId) && !isAdmin) {
            throw new SecurityException("Only creator or admin can delete the study");
        }

        // 1. 모든 멤버를 각자의 Personal 스터디로 쫓아냄
        List<User> members = userRepository.findByStudyId(studyId);
        for (User member : members) {
            // Group 스터디 관계 끊기
            member.updateStudy(null);
            userRepository.update(member);

            // 각 멤버를 위한 Personal 스터디 생성
            Study personalStudy = createPersonalStudy(member.getId());

            // 멤버의 기록을 삭제되는 Group 스터디에서 새로운 Personal 스터디로 이관
            algorithmRecordService.migrateUserRecords(member.getId(), studyId, personalStudy.getId());
        }

        // 2. 스터디 삭제 (신청 내역이나 미션 등은 DB의 CASCADE 옵션 또는 수동 처리가 필요할 수 있음)
        studyRepository.delete(studyId);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getStudyMembers(Long studyId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("Study not found"));

        return userRepository.findByStudyId(studyId).stream()
                .map(user -> UserResult.from(user, null, study))
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public void delegateLeader(Long currentLeaderId, Long studyId, Long newLeaderId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("Study not found"));

        if (!Objects.equals(study.getCreatorId(), currentLeaderId)) {
            throw new SecurityException("Only creator can delegate the leader role");
        }

        User newLeader = userRepository.findById(newLeaderId)
                .orElseThrow(() -> new IllegalArgumentException("New leader not found"));

        if (!Objects.equals(newLeader.getStudyId(), studyId)) {
            throw new IllegalArgumentException("New leader must be a member of the study");
        }

        study.setCreatorId(newLeaderId);
        studyRepository.update(study);

        // Notify new leader
        notificationService.send(
                newLeaderId,
                String.format("'%s' 스터디의 스터디장으로 임명되었습니다.", study.getName()),
                "/user/profile",
                NotificationType.STUDY_RESULT);
    }

    @Transactional(readOnly = true)
    public StudyApplication getApplicationDetail(Long userId, Long applicationId) {
        StudyApplication app = studyRepository.findApplicationById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        Study study = studyRepository.findById(app.getStudyId())
                .orElseThrow(() -> new IllegalArgumentException("Study not found"));

        if (!Objects.equals(study.getCreatorId(), userId)) {
            throw new SecurityException("Only creator can view application details");
        }

        return app;
    }

}
