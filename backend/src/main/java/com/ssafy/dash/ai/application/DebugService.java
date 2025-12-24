package com.ssafy.dash.ai.application;

import com.ssafy.dash.ai.client.AiServerClient;
import com.ssafy.dash.ai.client.dto.AiCounterExampleRequest;
import com.ssafy.dash.ai.client.dto.AiCounterExampleResponse;
import com.ssafy.dash.ai.client.dto.AiSimulatorRequest;
import com.ssafy.dash.ai.client.dto.AiSimulatorResponse;
import com.ssafy.dash.ai.client.dto.AiSimulatorResponse;
import com.ssafy.dash.algorithm.domain.AlgorithmRecord;
import com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DebugService {

    private final AiServerClient aiServerClient;
    private final AlgorithmRecordRepository algorithmRecordRepository;
    private final CodeReviewService codeReviewService;

    @Transactional
    public AiCounterExampleResponse generateCounterExample(Long recordId, String problemNumber, String code,
            String language,
            String platform, String problemTitle) {

        // 1. Check DB Cache
        if (recordId != null) {
            try {
                AlgorithmRecord record = algorithmRecordRepository.findById(recordId).orElse(null);
                if (record != null && record.getCounterExampleInput() != null
                        && !record.getCounterExampleInput().isBlank()) {
                    return new AiCounterExampleResponse(
                            record.getCounterExampleInput(),
                            record.getCounterExampleExpected(),
                            record.getCounterExamplePredicted(),
                            record.getCounterExampleReason());
                }
            } catch (Exception e) {
                // Ignore caching errors
            }
        }

        AiCounterExampleRequest request = new AiCounterExampleRequest(
                recordId, problemNumber, code, language,
                platform, problemTitle, null);
        AiCounterExampleResponse response = aiServerClient.generateCounterExample(request);

        // 2. Save to DB Update
        if (recordId != null) {
            try {
                codeReviewService.saveCounterExample(recordId, response);
            } catch (Exception e) {
                // Ignore save errors
            }
        }

        return response;
    }

    public AiSimulatorResponse simulate(String code, String language) {
        return aiServerClient.simulate(AiSimulatorRequest.builder()
                .code(code)
                .language(language)
                .build());
    }
}
