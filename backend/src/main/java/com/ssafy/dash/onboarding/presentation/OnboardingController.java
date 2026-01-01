package com.ssafy.dash.onboarding.presentation;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;
import com.ssafy.dash.oauth.application.OAuthTokenService;
import com.ssafy.dash.oauth.domain.UserOAuthToken;
import com.ssafy.dash.onboarding.application.OnboardingService;
import com.ssafy.dash.onboarding.application.dto.command.RepositorySetupCommand;
import com.ssafy.dash.onboarding.application.dto.result.RepositorySetupResult;
import com.ssafy.dash.onboarding.presentation.dto.request.RepositorySetupRequest;
import com.ssafy.dash.onboarding.presentation.dto.response.RepositorySetupResponse;
import com.ssafy.dash.onboarding.presentation.dto.response.RepositorySearchResponse;
import com.ssafy.dash.onboarding.presentation.dto.response.SolvedacVerifyResponse;
import com.ssafy.dash.solvedac.domain.SolvedacApiClient;
import com.ssafy.dash.solvedac.domain.dto.SolvedacUserResponse;
import com.ssafy.dash.solvedac.domain.exception.SolvedacApiException;
import com.ssafy.dash.github.domain.GitHubClient;
import com.ssafy.dash.github.domain.RepositoryInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import com.ssafy.dash.analytics.application.SolvedacSyncService;
import com.ssafy.dash.analytics.application.dto.request.RegisterHandleRequest;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final SolvedacSyncService solvedacSyncService;
    private final SolvedacApiClient solvedacApiClient;
    private final GitHubClient gitHubClient;
    private final OAuthTokenService oAuthTokenService;

    public OnboardingController(OnboardingService onboardingService, SolvedacSyncService solvedacSyncService,
            SolvedacApiClient solvedacApiClient, GitHubClient gitHubClient, OAuthTokenService oAuthTokenService) {
        this.onboardingService = onboardingService;
        this.solvedacSyncService = solvedacSyncService;
        this.solvedacApiClient = solvedacApiClient;
        this.gitHubClient = gitHubClient;
        this.oAuthTokenService = oAuthTokenService;
    }

    @GetMapping("/solvedac/verify")
    @Operation(summary = "Solved.ac 핸들 검증", description = "Solved.ac 아이디가 유효한지 확인하고 유저 정보를 반환합니다.")
    public ResponseEntity<SolvedacVerifyResponse> verifySolvedacHandle(
            @RequestParam String handle) {
        try {
            SolvedacUserResponse userResponse = solvedacApiClient.getUserInfo(handle.trim());
            return ResponseEntity.ok(SolvedacVerifyResponse.from(userResponse));
        } catch (SolvedacApiException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/repository/search")
    @Operation(summary = "GitHub 저장소 검색", description = "사용자의 GitHub 저장소 중 검색어에 매칭되는 목록을 반환합니다.")
    public ResponseEntity<List<RepositorySearchResponse>> searchRepositories(
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal,
            @RequestParam(required = false, defaultValue = "") String q) {
        if (principal instanceof CustomOAuth2User customUser) {
            try {
                UserOAuthToken token = oAuthTokenService.requireValidAccessToken(customUser.getUserId());
                List<RepositoryInfo> repos = gitHubClient.listUserRepositories(token.getAccessToken());

                String query = q.trim().toLowerCase();
                List<RepositorySearchResponse> filtered = repos.stream()
                        .filter(repo -> query.isEmpty() || repo.name().toLowerCase().contains(query))
                        .map(RepositorySearchResponse::from)
                        .collect(Collectors.toList());

                return ResponseEntity.ok(filtered);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/solvedac")
    @Operation(summary = "Solved.ac 핸들 등록", description = "Solved.ac 아이디를 연동하고 데이터를 분석합니다.")
    public ResponseEntity<Void> registerSolvedacRaw(
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal,
            @Validated @RequestBody RegisterHandleRequest request) {
        if (principal instanceof CustomOAuth2User customUser) {
            solvedacSyncService.registerSolvedacHandle(customUser.getUserId(), request.getHandle(),
                    request.getProfileImageUrl());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/repository")
    @Operation(summary = "온보딩 저장소 설정", description = "신규 가입자가 사용할 GitHub 저장소를 등록합니다.")
    public ResponseEntity<RepositorySetupResponse> setupRepository(
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal,
            @Validated @RequestBody RepositorySetupRequest request) {
        if (principal instanceof CustomOAuth2User customUser) {
            RepositorySetupCommand command = request.toCommand(customUser.getUserId());
            RepositorySetupResult result = onboardingService.setupRepository(command);

            return ResponseEntity.ok(RepositorySetupResponse.from(result));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
