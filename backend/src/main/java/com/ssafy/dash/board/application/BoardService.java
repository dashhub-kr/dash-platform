package com.ssafy.dash.board.application;

import com.ssafy.dash.board.application.dto.command.BoardCreateCommand;
import com.ssafy.dash.board.application.dto.command.BoardUpdateCommand;
import com.ssafy.dash.board.application.dto.result.BoardResult;
import com.ssafy.dash.board.domain.Board;
import com.ssafy.dash.board.domain.BoardRepository;
import com.ssafy.dash.board.domain.exception.BoardNotFoundException;
import com.ssafy.dash.common.exception.UnauthorizedAccessException;
import com.ssafy.dash.like.application.LikeService;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.user.domain.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final com.ssafy.dash.comment.application.CommentService commentService;
    private final LikeService likeService;

    public BoardService(BoardRepository boardRepository, UserRepository userRepository,
            com.ssafy.dash.comment.application.CommentService commentService,
            LikeService likeService) {
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
        this.commentService = commentService;
        this.likeService = likeService;
    }

    @Transactional
    public BoardResult create(BoardCreateCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        LocalDateTime now = LocalDateTime.now();
        Board board = Board.create(
                command.userId(),
                command.title(),
                command.content(),
                command.algorithmRecordId(),
                command.boardType(),
                now);

        boardRepository.save(board);

        if (command.initialComments() != null) {
            for (var commentCmd : command.initialComments()) {
                commentService.create(new com.ssafy.dash.comment.application.dto.command.CommentCreateCommand(
                        board.getId(),
                        command.userId(),
                        null,
                        commentCmd.lineNumber(),
                        commentCmd.content()));
            }
        }

        return toResult(board, user, false);
    }

    @Transactional(readOnly = true)
    public BoardResult findById(Long id, Long requestUserId) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new BoardNotFoundException(id));

        User user = userRepository.findById(board.getUserId()).orElse(null);
        boolean isLiked = (requestUserId != null) && likeService.isLikedBoard(id, requestUserId);

        return toResult(board, user, isLiked);
    }

    @Transactional(readOnly = true)
    public List<BoardResult> findAll(Long requestUserId) {

        return boardRepository.findAll().stream()
                .map(board -> {
                    User user = userRepository.findById(board.getUserId()).orElse(null);
                    boolean isLiked = (requestUserId != null) && likeService.isLikedBoard(board.getId(), requestUserId);
                    return toResult(board, user, isLiked);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public BoardResult update(Long id, BoardUpdateCommand command, Long requestUserId) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new BoardNotFoundException(id));

        validateOwnership(board, requestUserId);

        LocalDateTime now = LocalDateTime.now();
        board.applyUpdate(command.title(), command.content(), command.algorithmRecordId(), now);

        boardRepository.update(board);

        User user = userRepository.findById(board.getUserId()).orElse(null);
        boolean isLiked = likeService.isLikedBoard(id, requestUserId);

        return toResult(board, user, isLiked);
    }

    @Transactional
    public void delete(Long id, Long requestUserId) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new BoardNotFoundException(id));

        validateOwnership(board, requestUserId);

        boardRepository.delete(id);
    }

    @Transactional
    public BoardResult like(Long boardId, Long userId) {
        likeService.likeBoard(boardId, userId);
        return findById(boardId, userId);
    }

    @Transactional
    public BoardResult unlike(Long boardId, Long userId) {
        likeService.unlikeBoard(boardId, userId);
        return findById(boardId, userId);
    }

    private void validateOwnership(Board board, Long requestUserId) {
        if (!board.getUserId().equals(requestUserId)) {
            throw new UnauthorizedAccessException("Board", board.getId(), requestUserId);
        }
    }

    private BoardResult toResult(Board board, User user, boolean isLiked) {
        String authorName = (user != null) ? user.getUsername() : "Unknown";

        return BoardResult.from(board, authorName, isLiked);
    }

}
