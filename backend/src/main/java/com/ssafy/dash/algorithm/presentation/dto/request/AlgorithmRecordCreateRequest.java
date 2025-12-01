package com.ssafy.dash.algorithm.presentation.dto.request;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.web.multipart.MultipartFile;

import com.ssafy.dash.algorithm.application.dto.command.AlgorithmRecordCreateCommand;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlgorithmRecordCreateRequest {

    private String problemNumber;
    private String title;
    private String language;
    private MultipartFile file;

    public AlgorithmRecordCreateCommand toCommand(Long userId) throws IOException {
        String code = "";
        if (file != null && !file.isEmpty()) {
            code = new String(file.getBytes(), StandardCharsets.UTF_8);
        }
        
        return new AlgorithmRecordCreateCommand(userId, problemNumber, title, language, code);
    }

}
