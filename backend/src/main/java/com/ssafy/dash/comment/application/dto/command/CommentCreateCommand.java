package com.ssafy.dash.comment.application.dto.command;

public record CommentCreateCommand(
        Long boardId,
        Long userId,
        Long parentId, // nullable - 대댓글용
        Integer lineNumber, // nullable - 라인 댓글용
        String content) {

}
