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
    private final com.ssafy.dash.acorn.domain.AcornLogRepository acornLogRepository;

    @Transactional(readOnly = true)
    public List<Study> getStudies(Long userId, String keyword) {
        // 1. 모든 스터디 조회 (또는 검색)
        List<Study> studies = (keyword != null && !keyword.isBlank())
                ? studyRepository.searchByKeyword(keyword)
                : studyRepository.findAll();

        // 2. 로그인하지 않은 유저라면 전체 목록 반환
        if (userId == null) {
            return studies;
        }

        // 3. 유저 정보 조회
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return studies;
        }

        // 4. 본인 스터디 제외 로직 제거.
        // 리더보드/전체 목록에 "내 스터디"가 표시되어야 합니다.
        // "추천 스터디"에서의 제외는 프론트엔드에서 처리합니다.
        
        return studies;
    }

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

        // 스터디 이동(Transition): 이미 다른 Group 스터디에 있다면, 탈퇴 또는 삭제 처리
        if (user.getStudyId() != null) {
            handleSeamlessTransition(user);
            // 상태 변경 후 유저 정보 갱신
            user = userRepository.findById(userId).orElseThrow();
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
             // 기존 Personal Study ID가 있으면 해당 ID의 기록을 모두 이동
             algorithmRecordService.migrateStudyId(oldStudy.getId(), study.getId());
             studyRepository.delete(oldStudy.getId());
        } else if (oldStudy == null) {
            // 이전에 스터디가 없었더라도(null -> Group), 유저의 모든 고아 기록을 새 스터디로 이동
            algorithmRecordService.migrateUserRecords(user.getId(), null, study.getId());
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

        // 스터디 이동 로직은 승인 시점에 처리됩니다. (applyForStudy에서는 체크하지 않음)

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

        // 유효성 검사 및 스터디 이동(Seamless Transition) 처리
        if (user.getStudyId() != null) {
            Study currentStudy = studyRepository.findById(user.getStudyId()).orElse(null);
            
            if (currentStudy != null && currentStudy.getStudyType() == StudyType.GROUP) {
                try {
                     // 기존 Group 스터디에서 나오기 (탈퇴/삭제)
                     handleSeamlessTransition(user);
                     // 유저 정보 갱신
                     user = userRepository.findById(user.getId()).orElseThrow();
                     
                     // 이제 Personal 스터디 상태가 되었으므로 아래 로직에서 마이그레이션 처리됨
                     currentStudy = studyRepository.findById(user.getStudyId()).orElse(null);
                } catch (IllegalStateException e) {
                     // 이동 실패 (예: 위임되지 않은 스터디장) -> 예외 전파
                     throw e; 
                }
            }

            // PERSONAL 스터디인 경우 - 기록 마이그레이션 (개인 스터디이거나, 스터디가 없던 상태일 수 있음)
            if (currentStudy == null || currentStudy.getStudyType() == StudyType.PERSONAL) {
                // 이전 스터디 ID가 뭐든간에(null 포함) 현재 가입하는 스터디로 모든 기록을 이동
                algorithmRecordService.migrateUserRecords(
                    user.getId(), 
                    currentStudy != null ? currentStudy.getId() : null, 
                    study.getId()
                );
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

        // 2. Acorn Log 삭제 (FK 제약조건 해결)
        acornLogRepository.deleteByStudyId(studyId);

        // 3. 스터디 삭제 (신청 내역이나 미션 등은 DB의 CASCADE 옵션 또는 수동 처리가 필요할 수 있음)
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

    // 유연한 스터디 이동을 위한 헬퍼 (Seamless Transition)
    // - 일반 멤버: 기존 스터디 탈퇴 후 새 스터디 가입
    // - 나홀로 스터디장: 기존 스터디 삭제 후 새 스터디 가입/생성
    // - 멤버 있는 스터디장: 예외 발생 (위임 필요)
    private void handleSeamlessTransition(User user) {
        if (user.getStudyId() == null) return;
        
        Study currentStudy = studyRepository.findById(user.getStudyId()).orElse(null);
        if (currentStudy == null || currentStudy.getStudyType() == StudyType.PERSONAL) return;

        // Group 스터디일 경우 처리
        if (Objects.equals(currentStudy.getCreatorId(), user.getId())) {
            // 스터디장인 경우
            int memberCount = userRepository.findByStudyId(currentStudy.getId()).size();
            if (memberCount > 1) {
                throw new IllegalStateException("스터디장은 바로 이동할 수 없습니다. 스터디장을 위임하거나 스터디원을 내보낸 후 다시 시도해주세요.");
            }
            // 나홀로 스터디장 -> 스터디 삭제 (Acorn Log 포함하여 안전하게 삭제)
            // deleteStudy 메서드 내부에서 Acorn Log 삭제 및 멤버(본인)의 Personal 스터디로의 이관이 수행됩니다.
            // 이후 호출 측에서 Personal 스터디 -> 새 스터디로 데이터를 다시 이관하게 됩니다.
            deleteStudy(user.getId(), currentStudy.getId());
        } else {
            // 일반 멤버 -> 탈퇴 (Personal 스터디로 이동)
            leaveStudy(user.getId());
        }
    }

}
