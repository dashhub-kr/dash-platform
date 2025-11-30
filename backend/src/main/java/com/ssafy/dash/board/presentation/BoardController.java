package com.ssafy.dash.board.presentation;

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
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.dash.board.application.BoardService;
import com.ssafy.dash.board.presentation.dto.request.BoardCreateRequest;
import com.ssafy.dash.board.presentation.dto.request.BoardUpdateRequest;
import com.ssafy.dash.board.presentation.dto.response.BoardResponse;

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

        return ResponseEntity.created(URI.create("/api/boards/" + response.getId())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BoardResponse>> findAll() {
        List<BoardResponse> responses = boardService.findAll().stream()
                .map(BoardResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardResponse> findById(@PathVariable Long id) {
        BoardResponse response = BoardResponse.from(boardService.findById(id));

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoardResponse> update(@PathVariable Long id, @RequestBody BoardUpdateRequest request) {
        BoardResponse response = BoardResponse.from(boardService.update(id, request.toCommand()));

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boardService.delete(id);
        
        return ResponseEntity.noContent().build();
    }

}
