package com.ssafy.dash.board.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.board.application.BoardService;
import com.ssafy.dash.board.application.dto.command.BoardCreateCommand;
import com.ssafy.dash.board.application.dto.command.BoardUpdateCommand;
import com.ssafy.dash.board.application.dto.result.BoardResult;
import com.ssafy.dash.board.domain.exception.BoardNotFoundException;
import com.ssafy.dash.board.presentation.dto.request.BoardCreateRequest;
import com.ssafy.dash.board.presentation.dto.request.BoardUpdateRequest;
import com.ssafy.dash.common.TestFixtures;
import com.ssafy.dash.common.fixtures.FixtureTime;
import com.ssafy.dash.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BoardController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(BoardControllerTest.TestConfig.class)
@DisplayName("BoardController 단위 테스트")
class BoardControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private BoardService boardService;

        @Autowired
        private ObjectMapper objectMapper;

        private BoardResult boardResult;
        private User user;

        @TestConfiguration
        static class TestConfig {
                @Bean
                public BoardService boardService() {
                        return Mockito.mock(BoardService.class);
                }
        }

        @BeforeEach
        void setUp() {
                user = TestFixtures.createUser();
                boardResult = TestFixtures.createBoardResult(user);
        }

        @AfterEach
        void tearDown() {
                Mockito.reset(boardService);
        }

        @Test
        @DisplayName("게시글 생성 요청이 성공하면 201을 반환한다")
        void createBoard_Success() throws Exception {
                BoardCreateRequest req = TestFixtures.createBoardCreateRequest();
                given(boardService.create(any(BoardCreateCommand.class))).willReturn(boardResult);

                mockMvc.perform(post("/api/boards")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(TestFixtures.TEST_BOARD_ID))
                                .andExpect(jsonPath("$.title").value(TestFixtures.TEST_BOARD_TITLE));

                verify(boardService).create(any(BoardCreateCommand.class));
        }

        @Test
        @DisplayName("전체 게시글을 조회하면 200과 목록을 반환한다")
        void getAllBoards_Success() throws Exception {
                given(boardService.findAll(anyLong())).willReturn(List.of(boardResult));

                mockMvc.perform(get("/api/boards"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(TestFixtures.TEST_BOARD_ID));

                verify(boardService).findAll(anyLong());
        }

        @Test
        @DisplayName("ID로 게시글을 조회하면 단일 결과를 반환한다")
        void getBoardById_Success() throws Exception {
                given(boardService.findById(eq(TestFixtures.TEST_BOARD_ID), anyLong())).willReturn(boardResult);

                mockMvc.perform(get("/api/boards/" + TestFixtures.TEST_BOARD_ID))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(TestFixtures.TEST_BOARD_ID));

                verify(boardService).findById(eq(TestFixtures.TEST_BOARD_ID), anyLong());
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 404를 반환한다")
        void getBoardById_Failure_NotFound() throws Exception {
                given(boardService.findById(eq(TestFixtures.TEST_BOARD_ID), anyLong()))
                                .willThrow(new BoardNotFoundException(TestFixtures.TEST_BOARD_ID));

                mockMvc.perform(get("/api/boards/" + TestFixtures.TEST_BOARD_ID))
                                .andExpect(status().isNotFound());

                verify(boardService).findById(eq(TestFixtures.TEST_BOARD_ID), anyLong());
        }

    @Test
    @DisplayName("게시글 수정 요청이 성공하면 변경 내용이 반영된다")
    void updateBoard_Success() throws Exception {
        BoardUpdateRequest req = TestFixtures.createBoardUpdateRequest();
        BoardResult updatedResult = new BoardResult(TestFixtures.TEST_BOARD_ID, req.getTitle(), req.getContent(),
                user.getId(), user.getUsername(), null, "GENERAL", 0, 0, false, FixtureTime.now(), FixtureTime.now());
                user.getId(), user.getUsername(), null, "GENERAL", 0, 0, false, FixtureTime.now(), FixtureTime.now());

        given(boardService.update(eq(TestFixtures.TEST_BOARD_ID), any(BoardUpdateCommand.class), anyLong()))
                .willReturn(updatedResult);

        mockMvc.perform(put("/api/boards/" + TestFixtures.TEST_BOARD_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(req.getTitle()));

        verify(boardService).update(eq(TestFixtures.TEST_BOARD_ID), any(BoardUpdateCommand.class), anyLong());
    }

        @Test
        @DisplayName("없는 게시글을 수정하면 404를 반환한다")
        void updateBoard_Failure_NotFound() throws Exception {
                BoardUpdateRequest req = TestFixtures.createBoardUpdateRequest();
                given(boardService.update(eq(TestFixtures.TEST_BOARD_ID), any(BoardUpdateCommand.class), anyLong()))
                                .willThrow(new BoardNotFoundException(TestFixtures.TEST_BOARD_ID));

                mockMvc.perform(put("/api/boards/" + TestFixtures.TEST_BOARD_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isNotFound());

                verify(boardService).update(eq(TestFixtures.TEST_BOARD_ID), any(BoardUpdateCommand.class), anyLong());
        }

        @Test
        @DisplayName("게시글 삭제에 성공하면 204를 반환한다")
        void deleteBoard_Success() throws Exception {
                mockMvc.perform(delete("/api/boards/" + TestFixtures.TEST_BOARD_ID))
                                .andExpect(status().isNoContent());

                verify(boardService).delete(eq(TestFixtures.TEST_BOARD_ID), anyLong());
        }

        @Test
        @DisplayName("삭제 대상이 없으면 404를 반환한다")
        void deleteBoard_Failure_NotFound() throws Exception {
                willThrow(new BoardNotFoundException(TestFixtures.TEST_BOARD_ID))
                                .given(boardService).delete(eq(TestFixtures.TEST_BOARD_ID), anyLong());

                mockMvc.perform(delete("/api/boards/" + TestFixtures.TEST_BOARD_ID))
                                .andExpect(status().isNotFound());

                verify(boardService).delete(eq(TestFixtures.TEST_BOARD_ID), anyLong());
        }

}
