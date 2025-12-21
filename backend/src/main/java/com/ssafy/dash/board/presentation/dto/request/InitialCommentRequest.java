package com.ssafy.dash.board.presentation.dto.request;

import com.ssafy.dash.board.application.dto.command.InitialCommentCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InitialCommentRequest {
    private Integer lineNumber;
    private String content;

    public InitialCommentCommand toCommand() {
        return new InitialCommentCommand(lineNumber, content);
    }
}
