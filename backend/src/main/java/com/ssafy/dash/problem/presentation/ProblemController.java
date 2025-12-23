package com.ssafy.dash.problem.presentation;

import com.ssafy.dash.problem.application.ProblemService;
import com.ssafy.dash.problem.domain.ProblemRecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping("/recommend")
    public ResponseEntity<List<ProblemRecommendationResponse>> recommendProblems(
            @RequestParam String tag,
            @RequestParam int tier,
            @AuthenticationPrincipal OAuth2User principal) {

        if (!(principal instanceof CustomOAuth2User customUser)) {
            return ResponseEntity.status(401).build();
        }

        Long userId = customUser.getUserId();
        List<ProblemRecommendationResponse> recommendations = problemService.getRecommendedProblems(tag, tier, userId);
        return ResponseEntity.ok(recommendations);
    }
}
