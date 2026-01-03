package com.ssafy.dash.common.fixtures;

import java.time.LocalDateTime;

import com.ssafy.dash.comment.application.dto.command.CommentCreateCommand;
import com.ssafy.dash.comment.application.dto.command.CommentUpdateCommand;
import com.ssafy.dash.comment.application.dto.result.CommentResult;
import com.ssafy.dash.comment.domain.Comment;
import com.ssafy.dash.user.domain.User;

public final class CommentFixtures {

    public static final Long TEST_COMMENT_ID = 1L;
    public static final Long TEST_BOARD_ID = 1L;
    public static final String TEST_COMMENT_CONTENT = "테스트 댓글 내용입니다.";
    public static final String UPDATED_COMMENT_CONTENT = "수정된 댓글 내용입니다.";

    private CommentFixtures() {
    }

    public static Comment createComment(Long boardId, User user) {
        return commentFixture(boardId, user).toDomain(FixtureTime.now());
    }

    public static Comment createReplyComment(Long boardId, User user, Long parentId) {
        return replyFixture(boardId, user, parentId).toDomain(FixtureTime.now());
    }

    public static Comment createLineComment(Long boardId, User user, Integer lineNumber) {
        return lineCommentFixture(boardId, user, lineNumber).toDomain(FixtureTime.now());
    }

    public static CommentCreateCommand createCommentCreateCommand(Long boardId, Long userId) {
        return new CommentCreateCommand(boardId, userId, null, null, TEST_COMMENT_CONTENT);
    }

    public static CommentCreateCommand createReplyCreateCommand(Long boardId, Long userId, Long parentId) {
        return new CommentCreateCommand(boardId, userId, parentId, null, TEST_COMMENT_CONTENT);
    }

    public static CommentCreateCommand createLineCommentCreateCommand(Long boardId, Long userId, Integer lineNumber) {
        return new CommentCreateCommand(boardId, userId, null, lineNumber, TEST_COMMENT_CONTENT);
    }

    public static CommentUpdateCommand createCommentUpdateCommand() {
        return new CommentUpdateCommand(UPDATED_COMMENT_CONTENT);
    }

    public static CommentResult createCommentResult(Long boardId, User user) {
        LocalDateTime timestamp = FixtureTime.now();
        return commentFixture(boardId, user).toResult(timestamp, timestamp);
    }

    public static CommentFixture commentFixture(Long boardId, User user) {
        return new CommentFixture(TEST_COMMENT_ID, boardId, user.getId(), user.getUsername(),
                null, null, TEST_COMMENT_CONTENT);
    }

    public static CommentFixture replyFixture(Long boardId, User user, Long parentId) {
        return new CommentFixture(TEST_COMMENT_ID + 1, boardId, user.getId(), user.getUsername(),
                parentId, null, "대댓글 내용입니다.");
    }

    public static CommentFixture lineCommentFixture(Long boardId, User user, Integer lineNumber) {
        return new CommentFixture(TEST_COMMENT_ID + 2, boardId, user.getId(), user.getUsername(),
                null, lineNumber, "라인 " + lineNumber + " 댓글입니다.");
    }

    public record CommentFixture(
            Long id,
            Long boardId,
            Long userId,
            String authorName,
            Long parentId,
            Integer lineNumber,
            String content) {

        public Comment toDomain(LocalDateTime timestamp) {
            Comment comment = Comment.create(boardId, userId, parentId, lineNumber, content, timestamp);
            comment.setId(id);
            return comment;
        }

        public CommentCreateCommand toCreateCommand() {
            return new CommentCreateCommand(boardId, userId, parentId, lineNumber, content);
        }

        public CommentResult toResult(LocalDateTime createdAt, LocalDateTime updatedAt) {
            return new CommentResult(id, boardId, userId, authorName, null, parentId, lineNumber,
                    content, 0, false, java.util.Collections.emptyList(), createdAt, updatedAt);
        }

    }

}
