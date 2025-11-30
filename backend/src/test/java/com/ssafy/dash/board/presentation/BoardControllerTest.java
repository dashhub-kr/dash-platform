package com.ssafy.dash.board.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.board.application.BoardService;
import com.ssafy.dash.board.application.dto.BoardCreateCommand;
import com.ssafy.dash.board.application.dto.BoardResult;
import com.ssafy.dash.board.application.dto.BoardUpdateCommand;
import com.ssafy.dash.board.presentation.dto.request.BoardCreateRequest;
import com.ssafy.dash.board.presentation.dto.request.BoardUpdateRequest;
import com.ssafy.dash.common.TestFixtures;
import com.ssafy.dash.user.domain.User;

@WebMvcTest(BoardController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(BoardControllerTest.TestConfig.class)
@DisplayName("BoardController 통합 테스트")
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BoardService boardService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public BoardService boardService() {
            
            return Mockito.mock(BoardService.class);
        }
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("게시글 생성 성공")
    void createBoard_Success() throws Exception {
        BoardCreateRequest req = TestFixtures.createBoardCreateRequest();
        User user = TestFixtures.createUser();
        BoardResult result = TestFixtures.createBoardResult(user);

        given(boardService.create(any(BoardCreateCommand.class))).willReturn(result);

        mockMvc.perform(post("/api/boards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TestFixtures.TEST_BOARD_ID))
                .andExpect(jsonPath("$.title").value(TestFixtures.TEST_BOARD_TITLE));
    }

    @Test
    @DisplayName("전체 게시글 조회 성공")
    void getAllBoards_Success() throws Exception {
        User user = TestFixtures.createUser();
        BoardResult result = TestFixtures.createBoardResult(user);
        given(boardService.findAll()).willReturn(List.of(result));

        mockMvc.perform(get("/api/boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(TestFixtures.TEST_BOARD_ID));
    }

    @Test
    @DisplayName("ID로 게시글 조회 성공")
    void getBoardById_Success() throws Exception {
        User user = TestFixtures.createUser();
        BoardResult result = TestFixtures.createBoardResult(user);
        given(boardService.findById(TestFixtures.TEST_BOARD_ID)).willReturn(result);

        mockMvc.perform(get("/api/boards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TestFixtures.TEST_BOARD_ID));
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updateBoard_Success() throws Exception {
        BoardUpdateRequest req = TestFixtures.createBoardUpdateRequest();
        User user = TestFixtures.createUser();
        BoardResult result = new BoardResult(TestFixtures.TEST_BOARD_ID, req.getTitle(), req.getContent(),
            user.getId(), user.getUsername(), LocalDateTime.now(), LocalDateTime.now());

        given(boardService.update(eq(TestFixtures.TEST_BOARD_ID), any(BoardUpdateCommand.class))).willReturn(result);

        mockMvc.perform(put("/api/boards/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(req.getTitle()));
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deleteBoard_Success() throws Exception {
        mockMvc.perform(delete("/api/boards/1"))
                .andExpect(status().isNoContent());
    }

}
