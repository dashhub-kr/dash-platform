package com.ssafy.dash.user.application;

import com.ssafy.dash.ai.infrastructure.persistence.LearningPathCacheMapper;
import com.ssafy.dash.study.domain.Study;
import com.ssafy.dash.study.domain.StudyRepository;
import com.ssafy.dash.user.application.dto.command.UserCreateCommand;
import com.ssafy.dash.user.application.dto.command.UserUpdateCommand;
import com.ssafy.dash.user.application.dto.result.UserResult;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.study.application.StudyService;
import com.ssafy.dash.onboarding.domain.OnboardingRepository;
import com.ssafy.dash.user.domain.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final OnboardingRepository onboardingRepository;
    private final StudyRepository studyRepository;
    private final StudyService studyService;
    private final LearningPathCacheMapper learningPathCacheMapper;

    public UserService(UserRepository userRepository,
            OnboardingRepository onboardingRepository,
            StudyRepository studyRepository,
            StudyService studyService,
            LearningPathCacheMapper learningPathCacheMapper) {
        this.userRepository = userRepository;
        this.onboardingRepository = onboardingRepository;
        this.studyRepository = studyRepository;
        this.studyService = studyService;
        this.learningPathCacheMapper = learningPathCacheMapper;
    }

    @Transactional
    public UserResult create(UserCreateCommand command) {
        User u = User.create(command.username(), command.email(), LocalDateTime.now(), null, null, null);
        userRepository.save(u);

        return UserResult.from(u);
    }

    @Transactional(readOnly = true)
    public UserResult findById(Long id) {
        User u = userRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        var onboarding = onboardingRepository.findByUserId(id).orElse(null);

        // 스터디 팀장 여부 확인
        Study study = null;
        if (u.getStudyId() != null) {
            study = studyRepository.findById(u.getStudyId()).orElse(null);
        }

        String pendingStudyName = null;
        var pendingApp = studyRepository.findPendingApplicationByUserId(id).orElse(null);
        if (pendingApp != null) {
            Study targetStudy = studyRepository.findById(pendingApp.getStudyId()).orElse(null);
            if (targetStudy != null) {
                pendingStudyName = targetStudy.getName();
            }
        }

        // 분석 데이터 존재 여부 확인
        boolean hasAnalysis = learningPathCacheMapper.findByUserId(id) != null;

        return UserResult.from(u, onboarding, study, pendingStudyName, hasAnalysis);
    }

    @Transactional(readOnly = true)
    public List<UserResult> findAll() {

        return userRepository.findAll().stream()
                .map(UserResult::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResult> searchByKeyword(String keyword) {
        return userRepository.searchByKeyword(keyword).stream()
                .map(UserResult::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResult update(Long id, UserUpdateCommand command) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        u.updateProfile(command.username(), command.email(), u.getAvatarUrl());
        userRepository.update(u);

        return UserResult.from(u);
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // 스터디 관련 처리
        if (user.getStudyId() != null) {
            Study study = studyRepository.findById(user.getStudyId()).orElse(null);

            // Group 스터디인 경우에만 체크 (Personal 스터디는 그냥 삭제됨)
            if (study != null && study.getStudyType() == Study.StudyType.GROUP) {
                if (java.util.Objects.equals(study.getCreatorId(), user.getId())) {
                    // 스터디장인 경우
                    int memberCount = userRepository.findByStudyId(study.getId()).size();
                    if (memberCount > 1) {
                        throw new IllegalStateException(
                                "스터디장은 다른 팀원이 있는 경우 탈퇴할 수 없습니다. 모든 팀원을 내보낸 후 다시 시도해주세요.");
                    }
                    // 혼자 남은 경우 -> 스터디 삭제 (안전)
                    studyService.deleteStudy(user.getId(), study.getId());
                } else {
                    // 일반 스터디원 -> 탈퇴 (Leave)
                    studyService.leaveStudy(user.getId());
                }
            }
        }

        boolean deleted = userRepository.delete(id);
        if (!deleted)
            throw new UserNotFoundException(id);
    }

    @Transactional
    public void blockUser(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        u.block();
        userRepository.update(u);
    }

}
