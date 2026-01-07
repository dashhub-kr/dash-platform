package com.ssafy.dash.comment.application;

import com.ssafy.dash.comment.application.dto.command.CommentCreateCommand;
import com.ssafy.dash.comment.application.dto.command.CommentUpdateCommand;
import com.ssafy.dash.comment.application.dto.result.CommentResult;
import com.ssafy.dash.comment.domain.Comment;
import com.ssafy.dash.comment.domain.CommentRepository;
import com.ssafy.dash.comment.domain.exception.CommentNotFoundException;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.notification.application.NotificationService;
import com.ssafy.dash.notification.domain.NotificationType;
import com.ssafy.dash.board.domain.Board;
import com.ssafy.dash.board.domain.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final BoardRepository boardRepository;

    public CommentService(CommentRepository commentRepository, UserRepository userRepository,
            NotificationService notificationService, BoardRepository boardRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.boardRepository = boardRepository;
    }

    @Transactional
    public CommentResult create(CommentCreateCommand command) {
        // 대댓글인 경우 부모 댓글이 최상위 댓글인지 확인 (1단계만 허용)
        if (command.parentId() != null) {
            Comment parent = commentRepository.findById(command.parentId())
                    .orElseThrow(() -> new CommentNotFoundException(command.parentId()));
            if (parent.getParentId() != null) {
                throw new IllegalArgumentException("Only one level of reply is allowed");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        Comment comment = Comment.create(
                command.boardId(),
                command.userId(),
                command.parentId(),
                command.lineNumber(),
                command.content(),
                now);

        commentRepository.save(comment);

        User user = userRepository.findById(command.userId()).orElse(null);
        if (user != null) {
            if (command.parentId() != null) {
                notifyParentAuthor(user, comment, command.parentId());
            } else {
                notifyBoardAuthor(user, comment, command.boardId());
            }
        }
        String authorName = (user != null) ? user.getUsername() : "Unknown";
        String authorProfileImageUrl = (user != null) ? user.getAvatarUrl() : null;
        String authorRole = (user != null) ? user.getRole() : null;

        return CommentResult.from(comment, authorName, authorProfileImageUrl, authorRole);
    }

    @Transactional(readOnly = true)
    public List<CommentResult> findByBoardId(Long boardId, Integer lineNumber, Long currentUserId) {
        List<Comment> comments;
        if (lineNumber != null) {
            comments = commentRepository.findByBoardIdAndLineNumber(boardId, lineNumber, currentUserId);
        } else {
            comments = commentRepository.findByBoardId(boardId, currentUserId);
        }

        return buildCommentTree(comments);
    }

    @Transactional(readOnly = true)
    public CommentResult findById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));

        User user = userRepository.findById(comment.getUserId()).orElse(null);
        String authorName = (user != null) ? user.getUsername() : "Unknown";
        String authorProfileImageUrl = (user != null) ? user.getAvatarUrl() : null;
        String authorRole = (user != null) ? user.getRole() : null;

        return CommentResult.from(comment, authorName, authorProfileImageUrl, authorRole);
    }

    @Transactional
    public CommentResult update(Long id, CommentUpdateCommand command) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));

        LocalDateTime now = LocalDateTime.now();
        comment.applyUpdate(command.content(), now);

        commentRepository.update(comment);

        User user = userRepository.findById(comment.getUserId()).orElse(null);
        String authorName = (user != null) ? user.getUsername() : "Unknown";
        String authorProfileImageUrl = (user != null) ? user.getAvatarUrl() : null;
        String authorRole = (user != null) ? user.getRole() : null;

        return CommentResult.from(comment, authorName, authorProfileImageUrl, authorRole);
    }

    @Transactional
    public void delete(Long id) {
        if (commentRepository.findById(id).isEmpty()) {
            throw new CommentNotFoundException(id);
        }
        commentRepository.delete(id);
    }

    /**
     * 댓글 목록을 트리 구조로 변환 (대댓글을 부모 댓글의 replies에 포함)
     */
    private List<CommentResult> buildCommentTree(List<Comment> comments) {
        Map<Long, List<Comment>> repliesMap = new HashMap<>();
        List<Comment> topLevel = new ArrayList<>();

        // 최상위 댓글과 대댓글을 분리
        for (Comment comment : comments) {
            if (comment.getParentId() == null) {
                topLevel.add(comment);
            } else {
                repliesMap.computeIfAbsent(comment.getParentId(), k -> new ArrayList<>()).add(comment);
            }
        }

        // 결과 생성
        return topLevel.stream()
                .map(comment -> {
                    String authorName = comment.getAuthorName() != null ? comment.getAuthorName() : "Unknown";
                    String authorProfileImageUrl = comment.getAuthorProfileImageUrl();
                    String authorRole = comment.getAuthorRole();
                    List<Comment> replies = repliesMap.getOrDefault(comment.getId(), List.of());
                    List<CommentResult> replyResults = replies.stream()
                            .map(reply -> {
                                String replyAuthor = reply.getAuthorName() != null ? reply.getAuthorName() : "Unknown";
                                String replyProfileImageUrl = reply.getAuthorProfileImageUrl();
                                String replyRole = reply.getAuthorRole();
                                return CommentResult.from(reply, replyAuthor, replyProfileImageUrl, replyRole);
                            })
                            .collect(Collectors.toList());
                    return CommentResult.from(comment, authorName, authorProfileImageUrl, authorRole, replyResults);
                })
                .collect(Collectors.toList());
    }

    private void notifyParentAuthor(User commenter, Comment comment, Long parentId) {
        if (parentId == null)
            return;

        Comment parent = commentRepository.findById(parentId).orElse(null);
        if (parent == null || parent.getUserId().equals(commenter.getId()))
            return; // Self reply check

        String message = String.format("%s님이 댓글에 답글을 남겼습니다.", commenter.getUsername());
        String url = String.format("/boards/%d", comment.getBoardId());

        notificationService.send(parent.getUserId(), message, url, NotificationType.COMMENT);
    }

    private void notifyBoardAuthor(User commenter, Comment comment, Long boardId) {
        Board board = boardRepository.findById(boardId).orElse(null);
        if (board == null || board.getUserId().equals(commenter.getId()))
            return;

        String message = String.format("%s님이 게시글에 댓글을 남겼습니다.", commenter.getUsername());
        String url = String.format("/boards/%d", boardId);

        notificationService.send(board.getUserId(), message, url, NotificationType.COMMENT);
    }

}
