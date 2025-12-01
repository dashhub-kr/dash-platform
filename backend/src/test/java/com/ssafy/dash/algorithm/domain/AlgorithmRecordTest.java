package com.ssafy.dash.algorithm.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ssafy.dash.common.fixtures.FixtureTime;

class AlgorithmRecordTest {

    @Test
    @DisplayName("생성 시 생성/수정 시간이 동일하게 초기화된다")
    void create_ShouldSetTimestamps() {
        LocalDateTime now = FixtureTime.now();

        AlgorithmRecord record = AlgorithmRecord.create(1L, "1000", "A+B", "Java",
                "print", now);

        assertThat(record.getCreatedAt()).isEqualTo(now);
        assertThat(record.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("부분 업데이트 시 전달된 필드만 변경된다")
    void applyUpdate_ShouldOverwriteProvidedFields() {
        AlgorithmRecord record = AlgorithmRecord.create(1L, "1000", "A+B", "Java",
                "code", FixtureTime.now());

        LocalDateTime updatedAt = FixtureTime.now().plusDays(1);
        record.applyUpdate("2000", null, null, "updated", updatedAt);

        assertThat(record.getProblemNumber()).isEqualTo("2000");
        assertThat(record.getTitle()).isEqualTo("A+B");
        assertThat(record.getLanguage()).isEqualTo("Java");
        assertThat(record.getCode()).isEqualTo("updated");
        assertThat(record.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("시간 정보 없이 업데이트하면 예외가 발생한다")
    void applyUpdate_ShouldValidateTimestamp() {
        AlgorithmRecord record = AlgorithmRecord.create(1L, "1000", "A+B", "Java",
                "code", FixtureTime.now());

        assertThatThrownBy(() -> record.applyUpdate("2000", "title", "Java", "code", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("잘못된 파라미터로 생성 시 예외가 발생한다")
    void create_ShouldValidateArguments() {
        assertThatThrownBy(() -> AlgorithmRecord.create(0L, "", "",
                "", null, FixtureTime.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
