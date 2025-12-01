package com.ssafy.dash.common.fixtures;

import java.time.LocalDateTime;

import com.ssafy.dash.algorithm.application.dto.command.AlgorithmRecordCreateCommand;
import com.ssafy.dash.algorithm.application.dto.command.AlgorithmRecordUpdateCommand;
import com.ssafy.dash.algorithm.application.dto.result.AlgorithmRecordResult;
import com.ssafy.dash.algorithm.domain.AlgorithmRecord;

public final class AlgorithmRecordFixtures {

    public static final Long TEST_ALGORITHM_RECORD_ID = 1L;
    public static final String TEST_PROBLEM_NUMBER = "1000";
    public static final String TEST_ALGORITHM_TITLE = "A+B";
    public static final String TEST_ALGORITHM_CODE = "import java.util.*; ...";
    public static final String TEST_ALGORITHM_LANGUAGE = "Java";

    private AlgorithmRecordFixtures() {
    }

    public static AlgorithmRecord createAlgorithmRecord(Long userId) {
        return algorithmRecordFixture(userId).toDomain(FixtureTime.now());
    }

    public static AlgorithmRecordCreateCommand createAlgorithmRecordCreateCommand(Long userId, String code) {
        return algorithmRecordFixture(userId).toCreateCommand(code);
    }

    public static AlgorithmRecordUpdateCommand createAlgorithmRecordUpdateCommand(Long userId, String code) {
        return algorithmRecordFixture(userId).toUpdateCommand(code);
    }

    public static AlgorithmRecordResult createAlgorithmRecordResult(Long userId) {
        LocalDateTime timestamp = FixtureTime.now();
        return algorithmRecordFixture(userId).toResult(timestamp, timestamp);
    }

    public static AlgorithmRecordFixture algorithmRecordFixture(Long userId) {
        return new AlgorithmRecordFixture(TEST_ALGORITHM_RECORD_ID, userId, TEST_PROBLEM_NUMBER, TEST_ALGORITHM_TITLE, TEST_ALGORITHM_CODE, TEST_ALGORITHM_LANGUAGE);
    }

    public record AlgorithmRecordFixture(
            Long id,
            Long userId,
            String problemNumber,
            String title,
            String code,
            String language
    ) {
        
        public AlgorithmRecord toDomain(LocalDateTime createdAt) {
            AlgorithmRecord record = AlgorithmRecord.create(userId, problemNumber, title,
                    language, code, createdAt);
            record.setId(id);
            return record;
        }

        public AlgorithmRecordResult toResult(LocalDateTime createdAt, LocalDateTime updatedAt) {
            return new AlgorithmRecordResult(id, userId, problemNumber, title, code, language,
                    createdAt, updatedAt);
        }

        public AlgorithmRecordCreateCommand toCreateCommand(String codeOverride) {
            return new AlgorithmRecordCreateCommand(userId, problemNumber, title, language,
                    codeOverride == null ? code : codeOverride);
        }

        public AlgorithmRecordUpdateCommand toUpdateCommand(String codeOverride) {
            return new AlgorithmRecordUpdateCommand(problemNumber, "Updated Title", language,
                    codeOverride == null ? code : codeOverride);
        }

    }   
    
}
