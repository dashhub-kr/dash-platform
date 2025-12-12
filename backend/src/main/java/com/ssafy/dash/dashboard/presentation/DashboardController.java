package com.ssafy.dash.dashboard.presentation;

import com.ssafy.dash.algorithm.application.AlgorithmRecordService;
import com.ssafy.dash.algorithm.application.dto.result.AlgorithmRecordResult;
import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.user.domain.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final AlgorithmRecordService algorithmRecordService;
    private final UserRepository userRepository;

    @GetMapping("/records")
    public ResponseEntity<List<AlgorithmRecordResult>> getDashboardRecords(
            @AuthenticationPrincipal CustomOAuth2User oauthUser) {
        
        Long userId = oauthUser.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Long studyId = user.getStudyId();
        
        List<AlgorithmRecordResult> records;
        if (studyId != null) {
            log.info("Fetching dashboard records for studyId: {}", studyId);
            records = algorithmRecordService.findByStudyId(studyId);
        } else {
            log.info("User {} has no study, fetching own records only", userId);
            records = algorithmRecordService.findByUserId(userId);
        }

        return ResponseEntity.ok(records);
    }
}
