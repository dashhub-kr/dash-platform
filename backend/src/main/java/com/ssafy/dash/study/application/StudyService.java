package com.ssafy.dash.study.application;

import com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository;
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
    private final AlgorithmRecordRepository algorithmRecordRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<Study> findAll() {
        return studyRepository.findAll();
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
            // PERSONAL 타입이면 아래에서 덮어쓰거나 처리?
            // createStudy는 새로운 GROUP 스터디를 만드는 것이므로,
            // PERSONAL 스터디를 가진 상태에서 새 스터디를 만들면 -> PERSONAL 스터디를 "삭제"하고 새 스터디로 이동?
            // 혹은 새 스터디 만들기를 허용하지 않을 수도 있음. (규칙: 스터디장은 탈퇴불가)
            // 여기서는 일단 기존 GROUP 스터디가 있으면 막는 것으로 유지.
            // PERSONAL 스터디가 있으면? 새 스터디 만들면 PERSONAL 스터디는 사라져야 함.
            if (currentStudy != null && currentStudy.getStudyType() == StudyType.PERSONAL) {
                // 개인 연구실 정리 후 생성 진행
                algorithmRecordRepository.migrateStudyId(currentStudy.getId(), null); // 임시로 null? 아니면 새 ID?
                // 새 ID는 save된 후에 나옴.
                // 따라서 createStudy 로직 중간에 마이그레이션이 필요함.
                // save 후에 처리해야 함.
            }
        }

        Study study = Study.create(name, userId);
        study.setDescription(description);
        study.setVisibility(visibility != null ? visibility : StudyVisibility.PUBLIC);
        study.setStudyType(StudyType.GROUP); // 기본은 GROUP

        studyRepository.save(study);

        // Store old study reference before updating user
        Long oldStudyId = user.getStudyId();
        Study oldStudy = oldStudyId != null ? studyRepository.findById(oldStudyId).orElse(null) : null;

        // Creator automatically joins (update user first to avoid FK constraint)
        user.updateStudy(study.getId());
        userRepository.update(user);

        // Now safe to delete old PERSONAL study (user no longer references it)
        if (oldStudy != null && oldStudy.getStudyType() == StudyType.PERSONAL) {
            algorithmRecordRepository.migrateStudyId(oldStudy.getId(), study.getId());
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

        // Check if study exists
        if (studyRepository.findById(studyId).isEmpty()) {
            throw new IllegalArgumentException("Study not found");
        }

        // Check if already applied
        studyRepository.findApplicationByStudyIdAndUserId(studyId, userId).ifPresent(app -> {
            if (app.getStatus() == StudyApplication.ApplicationStatus.PENDING) {
                throw new IllegalStateException("Already applied to this study");
            }
            // If not pending (e.g. APPROVED but left study), delete old application record
            studyRepository.deleteApplication(app.getId());
        });

        StudyApplication application = StudyApplication.create(studyId, userId, message);
        studyRepository.saveApplication(application);

        // Notify Study Leader
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

        application.approve();
        studyRepository.updateApplicationStatus(application);

        // Add user to study
        User user = userRepository.findById(application.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Migration Check
        if (user.getStudyId() != null) {
            Study oldStudy = studyRepository.findById(user.getStudyId()).orElse(null);
            if (oldStudy != null && oldStudy.getStudyType() == StudyType.PERSONAL) {
                // Migrate records
                algorithmRecordRepository.migrateStudyId(oldStudy.getId(), study.getId());
                // Delete old study
                studyRepository.delete(oldStudy.getId());
            }
        }

        user.updateStudy(study.getId());
        userRepository.update(user);

        // Notify Applicant
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
            // Policy: Creator cannot leave the study. They must delete it or transfer
            // ownership (not yet implemented).
            throw new IllegalStateException("스터디장은 탈퇴할 수 없습니다. 스터디를 해체하거나 권한을 위임해야 합니다.");
        }

        // 1. Leave Group Study
        user.updateStudy(null);
        userRepository.update(user);

        // 2. Fallback to Personal Study
        Study personalStudy = createPersonalStudy(userId);

        // 3. Migrate user's records from Group to Personal
        algorithmRecordRepository.migrateUserRecords(userId, oldStudyId, personalStudy.getId());
    }

    @Transactional(readOnly = true)
    public StudyStatsResult getStudyStats(Long studyId) {
        StudyStats stats = algorithmRecordRepository.countsByStudyId(studyId);
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

        if (!Objects.equals(study.getCreatorId(), userId)) {
            throw new SecurityException("Only creator can delete the study");
        }

        // 1. Migrate all members to Personal Studies
        List<User> members = userRepository.findByStudyId(studyId);
        for (User member : members) {
            // Clear group study association
            member.updateStudy(null);
            userRepository.update(member);

            // Create Personal Study for each member
            Study personalStudy = createPersonalStudy(member.getId());

            // Migrate member's records from deleted group to new personal study
            algorithmRecordRepository.migrateUserRecords(member.getId(), studyId, personalStudy.getId());
        }

        // 2. Delete study (Applications and Missions should be handled by DB ON DELETE
        // CASCADE or manually if needed)
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
