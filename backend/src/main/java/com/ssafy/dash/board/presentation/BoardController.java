package com.ssafy.dash.board.presentation;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.dash.board.presentation.dto.BoardCreateRequest;
import com.ssafy.dash.board.presentation.dto.BoardResponse;
import com.ssafy.dash.board.presentation.dto.BoardUpdateRequest;
import com.ssafy.dash.board.application.BoardService;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping
    public ResponseEntity<BoardResponse> create(@RequestBody BoardCreateRequest req) {
        BoardResponse response = boardService.create(req);

        return ResponseEntity.created(URI.create("/api/boards/" + response.getId())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BoardResponse>> findAll() {

        return ResponseEntity.ok(boardService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardResponse> findById(@PathVariable Long id) {

        return ResponseEntity.ok(boardService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoardResponse> update(@PathVariable Long id, @RequestBody BoardUpdateRequest req) {
        
        return ResponseEntity.ok(boardService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boardService.delete(id);
        
        return ResponseEntity.noContent().build();
    }

}
