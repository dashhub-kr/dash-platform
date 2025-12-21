package com.ssafy.dash.comment.presentation.dto.request;

import com.ssafy.dash.comment.application.dto.command.CommentUpdateCommand;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdateRequest {

    private String content;

    public CommentUpdateCommand toCommand() {
        return new CommentUpdateCommand(content);
    }

}
