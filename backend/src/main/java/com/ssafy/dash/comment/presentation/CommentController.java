package com.ssafy.dash.comment.presentation;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.dash.comment.application.CommentService;
import com.ssafy.dash.comment.presentation.dto.request.CommentCreateRequest;
import com.ssafy.dash.comment.presentation.dto.request.CommentUpdateRequest;
import com.ssafy.dash.comment.presentation.dto.response.CommentResponse;
import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RestController
@RequestMapping("/api/boards/{boardId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> create(
            @PathVariable Long boardId,
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal,
            @RequestBody CommentCreateRequest request) {

        Long userId = 1L;
        if (principal instanceof CustomOAuth2User customUser) {
            userId = customUser.getUserId();
        }

        CommentResponse response = CommentResponse.from(
                commentService.create(request.toCommand(boardId, userId)));

        return ResponseEntity.created(URI.create("/api/boards/" + boardId + "/comments/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> findByBoardId(
            @PathVariable Long boardId,
            @RequestParam(required = false) Integer lineNumber,
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal) {

        Long userId = null;
        if (principal instanceof CustomOAuth2User customUser) {
            userId = customUser.getUserId();
        }

        List<CommentResponse> responses = commentService.findByBoardId(boardId, lineNumber, userId).stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> update(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequest request) {

        CommentResponse response = CommentResponse.from(
                commentService.update(commentId, request.toCommand()));

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long boardId,
            @PathVariable Long commentId) {

        commentService.delete(commentId);

        return ResponseEntity.noContent().build();
    }

}
