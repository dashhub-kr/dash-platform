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
            throw new IllegalStateException("User already belongs to a study");
        }

        Study study = Study.create(name, userId);
        study.setDescription(description);
        study.setVisibility(visibility != null ? visibility : StudyVisibility.PUBLIC);

        studyRepository.save(study);

        // Creator automatically joins
        user.updateStudy(study.getId());
        userRepository.update(user);

        return study;
    }

    @Transactional
    public void applyForStudy(Long userId, Long studyId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getStudyId() != null) {
            throw new IllegalStateException("User already belongs to a study");
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

        Study study = studyRepository.findById(user.getStudyId())
                .orElseThrow(() -> new IllegalArgumentException("Study not found"));

        if (Objects.equals(study.getCreatorId(), userId)) {
            // Policy: Creator cannot leave the study. They must delete it or transfer
            // ownership (not yet implemented).
            throw new IllegalStateException("스터디장은 탈퇴할 수 없습니다. 스터디를 해체하거나 권한을 위임해야 합니다.");
        }

        user.updateStudy(null);
        userRepository.update(user);
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

        // 1. Remove all members
        List<User> members = userRepository.findByStudyId(studyId);
        for (User member : members) {
            member.updateStudy(null);
            userRepository.update(member);
        }

        // 2. Delete study (Applications and Missions should be handled by DB ON DELETE
        // CASCADE or manually if needed)
        // For safe implementation in limited context, we rely on DB definition.
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
