package com.ssafy.dash.algorithm.application.dto.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AlgorithmRecordUpdateCommand {

    private final String problemNumber;
    private final String title;
    private final String language;
    private final String code;

}
