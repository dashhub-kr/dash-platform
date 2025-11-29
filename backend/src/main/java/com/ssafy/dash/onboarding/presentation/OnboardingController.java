package com.ssafy.dash.onboarding.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.dash.oauth.infrastructure.security.CustomOAuth2User;
import com.ssafy.dash.onboarding.application.OnboardingService;
import com.ssafy.dash.onboarding.application.dto.RepositorySetupCommand;
import com.ssafy.dash.onboarding.application.dto.RepositorySetupResult;
import com.ssafy.dash.onboarding.presentation.dto.request.RepositorySetupRequest;
import com.ssafy.dash.onboarding.presentation.dto.response.RepositorySetupResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping("/repository")
    @Operation(summary = "온보딩 저장소 설정", description = "신규 가입자가 사용할 GitHub 저장소를 등록합니다.")
    public ResponseEntity<RepositorySetupResponse> setupRepository(
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal,
            @Validated @RequestBody RepositorySetupRequest request) {
        if (principal instanceof CustomOAuth2User customUser) {
            RepositorySetupCommand command = new RepositorySetupCommand(customUser.getUserId(), request.getRepositoryName());
            RepositorySetupResult result = onboardingService.setupRepository(command);

            return ResponseEntity.ok(RepositorySetupResponse.from(result));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
