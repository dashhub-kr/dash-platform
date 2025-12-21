package com.ssafy.dash.comment.application;

import com.ssafy.dash.comment.application.dto.command.CommentCreateCommand;
import com.ssafy.dash.comment.application.dto.command.CommentUpdateCommand;
import com.ssafy.dash.comment.application.dto.result.CommentResult;
import com.ssafy.dash.comment.domain.Comment;
import com.ssafy.dash.comment.domain.CommentRepository;
import com.ssafy.dash.comment.domain.exception.CommentNotFoundException;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
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

    public CommentService(CommentRepository commentRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
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
        String authorName = (user != null) ? user.getUsername() : "Unknown";

        return CommentResult.from(comment, authorName);
    }

    @Transactional(readOnly = true)
    public List<CommentResult> findByBoardId(Long boardId, Integer lineNumber) {
        List<Comment> comments;
        if (lineNumber != null) {
            comments = commentRepository.findByBoardIdAndLineNumber(boardId, lineNumber);
        } else {
            comments = commentRepository.findByBoardId(boardId);
        }

        return buildCommentTree(comments);
    }

    @Transactional(readOnly = true)
    public CommentResult findById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));

        User user = userRepository.findById(comment.getUserId()).orElse(null);
        String authorName = (user != null) ? user.getUsername() : "Unknown";

        return CommentResult.from(comment, authorName);
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

        return CommentResult.from(comment, authorName);
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
                    List<Comment> replies = repliesMap.getOrDefault(comment.getId(), List.of());
                    List<CommentResult> replyResults = replies.stream()
                            .map(reply -> {
                                String replyAuthor = reply.getAuthorName() != null ? reply.getAuthorName() : "Unknown";
                                return CommentResult.from(reply, replyAuthor);
                            })
                            .collect(Collectors.toList());
                    return CommentResult.from(comment, authorName, replyResults);
                })
                .collect(Collectors.toList());
    }

}
