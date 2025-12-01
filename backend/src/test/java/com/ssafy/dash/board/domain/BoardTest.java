package com.ssafy.dash.board.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ssafy.dash.common.fixtures.FixtureTime;

class BoardTest {

    @Test
    @DisplayName("생성 시 생성/수정 시간이 동일하게 초기화된다")
    void create_ShouldSetTimestamps() {
        LocalDateTime now = FixtureTime.now();

        Board board = Board.create(1L, "Title", "Content", now);

        assertThat(board.getCreatedAt()).isEqualTo(now);
        assertThat(board.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("부분 업데이트 시 전달된 필드만 변경된다")
    void applyUpdate_ShouldOverwriteProvidedFields() {
        Board board = Board.create(1L, "Title", "Content", FixtureTime.now());

        LocalDateTime updatedAt = FixtureTime.now().plusDays(1);
        board.applyUpdate("New Title", null, updatedAt);

        assertThat(board.getTitle()).isEqualTo("New Title");
        assertThat(board.getContent()).isEqualTo("Content");
        assertThat(board.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("시간 정보 없이 업데이트하면 예외가 발생한다")
    void applyUpdate_ShouldValidateTimestamp() {
        Board board = Board.create(1L, "Title", "Content", FixtureTime.now());

        assertThatThrownBy(() -> board.applyUpdate("New Title", "New Content", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("잘못된 파라미터로 생성 시 예외가 발생한다")
    void create_ShouldValidateArguments() {
        assertThatThrownBy(() -> Board.create(0L, "", "", FixtureTime.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
