package com.ssafy.dash.board.application.dto.command;

public record InitialCommentCommand(
        Integer lineNumber,
        String content) {
}
