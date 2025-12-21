package com.ssafy.dash.board.presentation.dto.request;

import com.ssafy.dash.board.application.dto.command.BoardCreateCommand;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardCreateRequest {

    private String title;
    private String content;
    private Long userId;
    private Long algorithmRecordId; // nullable
    private String boardType; // GENERAL | CODE_REVIEW
    private java.util.List<InitialCommentRequest> initialComments;

    public BoardCreateCommand toCommand() {
        var commandComments = (initialComments != null)
                ? initialComments.stream().map(InitialCommentRequest::toCommand).toList()
                : java.util.Collections.<com.ssafy.dash.board.application.dto.command.InitialCommentCommand>emptyList();

        return new BoardCreateCommand(title, content, userId, algorithmRecordId, boardType, commandComments);
    }

}
