package com.ssafy.dash.ai.application;

import com.ssafy.dash.ai.client.AiServerClient;
import com.ssafy.dash.ai.client.dto.AiCounterExampleRequest;
import com.ssafy.dash.ai.client.dto.AiCounterExampleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DebugService {

    private final AiServerClient aiServerClient;

    public AiCounterExampleResponse generateCounterExample(String problemNumber, String code, String language) {
        AiCounterExampleRequest request = new AiCounterExampleRequest(problemNumber, code, language, null);
        return aiServerClient.generateCounterExample(request);
    }
}
