package com.ssafy.dash.comment.presentation.dto.request;

import com.ssafy.dash.comment.application.dto.command.CommentCreateCommand;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {

    private String content;
    private Integer lineNumber; // nullable
    private Long parentId; // nullable

    public CommentCreateCommand toCommand(Long boardId, Long userId) {
        return new CommentCreateCommand(boardId, userId, parentId, lineNumber, content);
    }

}
