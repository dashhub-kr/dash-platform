package com.ssafy.dash.board.presentation;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.dash.board.application.BoardService;
import com.ssafy.dash.board.presentation.dto.request.BoardCreateRequest;
import com.ssafy.dash.board.presentation.dto.request.BoardUpdateRequest;
import com.ssafy.dash.board.presentation.dto.response.BoardResponse;
import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping
    public ResponseEntity<BoardResponse> create(@RequestBody BoardCreateRequest request) {
        BoardResponse response = BoardResponse.from(boardService.create(request.toCommand()));

        return ResponseEntity.created(URI.create("/api/boards/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BoardResponse>> findAll(
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal) {
        Long userId = extractUserIdNullable(principal);
        List<BoardResponse> responses = boardService.findAll(userId).stream()
                .map(BoardResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardResponse> findById(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal) {
        Long userId = extractUserIdNullable(principal);
        BoardResponse response = BoardResponse.from(boardService.findById(id, userId));

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoardResponse> update(
            @PathVariable Long id,
            @RequestBody BoardUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal) {

        Long userId = extractUserId(principal);
        BoardResponse response = BoardResponse.from(boardService.update(id, request.toCommand(), userId));

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal) {

        Long userId = extractUserId(principal);
        boardService.delete(id, userId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<BoardResponse> like(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal) {

        Long userId = extractUserId(principal);
        BoardResponse response = BoardResponse.from(boardService.like(id, userId));

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<BoardResponse> unlike(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal) {

        Long userId = extractUserId(principal);
        BoardResponse response = BoardResponse.from(boardService.unlike(id, userId));

        return ResponseEntity.ok(response);
    }

    private Long extractUserIdNullable(OAuth2User principal) {
        if (principal instanceof CustomOAuth2User customUser) {
            return customUser.getUserId();
        }
        return null; // Guest or not authenticated properly
    }

    private Long extractUserId(OAuth2User principal) {
        if (principal instanceof CustomOAuth2User customUser) {
            return customUser.getUserId();
        }
        return 1L; // Fallback for testing
    }

}
