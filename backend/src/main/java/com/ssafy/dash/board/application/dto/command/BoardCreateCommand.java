package com.ssafy.dash.board.application.dto.command;

public record BoardCreateCommand(
        String title, String content, Long userId
) {

}
