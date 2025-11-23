package com.ssafy.dash.board.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dash.board.domain.Board;
import com.ssafy.dash.board.dto.BoardCreateRequest;
import com.ssafy.dash.board.dto.BoardResponse;
import com.ssafy.dash.board.dto.BoardUpdateRequest;
import com.ssafy.dash.board.exception.BoardNotFoundException;
import com.ssafy.dash.board.mapper.BoardMapper;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.exception.UserNotFoundException;
import com.ssafy.dash.user.mapper.UserMapper;

@Service
public class BoardService {

    private final BoardMapper boardMapper;
    private final UserMapper userMapper;

    public BoardService(BoardMapper boardMapper, UserMapper userMapper) {
        this.boardMapper = boardMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public BoardResponse create(BoardCreateRequest req) {
        User user = userMapper.selectById(req.getUserId());
        if (user == null) {
            throw new UserNotFoundException(req.getUserId());
        }

        Board board = new Board();
        board.setTitle(req.getTitle());
        board.setContent(req.getContent());
        board.setUserId(req.getUserId());
        board.setCreatedAt(LocalDateTime.now());
        board.setUpdatedAt(LocalDateTime.now());

        boardMapper.insert(board);

        return toResponse(board, user);
    }

    @Transactional(readOnly = true)
    public BoardResponse findById(Long id) {
        Board board = boardMapper.selectById(id);
        if (board == null) {

            throw new BoardNotFoundException(id);
        }
        User user = userMapper.selectById(board.getUserId());

        return toResponse(board, user);
    }

    @Transactional(readOnly = true)
    public List<BoardResponse> findAll() {

        return boardMapper.selectAll().stream()
                .map(board -> {
                    User user = userMapper.selectById(board.getUserId());

                    return toResponse(board, user);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public BoardResponse update(Long id, BoardUpdateRequest req) {
        Board board = boardMapper.selectById(id);
        if (board == null) {
            throw new BoardNotFoundException(id);
        }

        board.setTitle(req.getTitle());
        board.setContent(req.getContent());
        board.setUpdatedAt(LocalDateTime.now());

        boardMapper.update(board);
        
        User user = userMapper.selectById(board.getUserId());

        return toResponse(board, user);
    }

    @Transactional
    public void delete(Long id) {
        int deleted = boardMapper.delete(id);
        if (deleted == 0) {
            throw new BoardNotFoundException(id);
        }
    }

    private BoardResponse toResponse(Board board, User user) {
        String authorName = (user != null) ? user.getUsername() : "Unknown";
        
        return new BoardResponse(
            board.getId(),
            board.getTitle(),
            board.getContent(),
            board.getUserId(),
            authorName,
            board.getCreatedAt(),
            board.getUpdatedAt()
        );
    }

}
