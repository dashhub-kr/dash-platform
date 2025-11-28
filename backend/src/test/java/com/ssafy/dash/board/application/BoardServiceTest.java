package com.ssafy.dash.board.application;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ssafy.dash.board.domain.Board;
import com.ssafy.dash.board.domain.BoardRepository;
import com.ssafy.dash.board.application.dto.BoardCreateRequest;
import com.ssafy.dash.board.application.dto.BoardResponse;
import com.ssafy.dash.board.application.dto.BoardUpdateRequest;
import com.ssafy.dash.common.TestFixtures;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.mapper.UserMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardService 단위 테스트")
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private BoardService boardService;

    private User user;
    private Board board;

    @BeforeEach
    void setUp() {
        user = TestFixtures.createUser();
        board = TestFixtures.createBoard(user);
    }

    @Test
    @DisplayName("게시글 생성 성공")
    void createBoard_Success() {
        // given
        BoardCreateRequest req = TestFixtures.createBoardCreateRequest();
        given(userMapper.selectById(req.getUserId())).willReturn(user);
        
        // when
        BoardResponse response = boardService.create(req);

        // then
        verify(boardRepository).save(any(Board.class));
        assertThat(response.getTitle()).isEqualTo(req.getTitle());
        assertThat(response.getAuthorName()).isEqualTo(user.getUsername());
    }

    @Test
    @DisplayName("ID로 게시글 조회 성공")
    void findById_Success() {
        // given
        given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));
        given(userMapper.selectById(user.getId())).willReturn(user);

        // when
        BoardResponse response = boardService.findById(board.getId());

        // then
        assertThat(response.getId()).isEqualTo(board.getId());
        assertThat(response.getAuthorName()).isEqualTo(user.getUsername());
    }

    @Test
    @DisplayName("전체 게시글 조회 성공")
    void findAll_Success() {
        // given
        given(boardRepository.findAll()).willReturn(List.of(board));
        given(userMapper.selectById(user.getId())).willReturn(user);

        // when
        List<BoardResponse> responses = boardService.findAll();

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getAuthorName()).isEqualTo(user.getUsername());
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updateBoard_Success() {
        // given
        BoardUpdateRequest req = TestFixtures.createBoardUpdateRequest();
        given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));
        given(userMapper.selectById(user.getId())).willReturn(user);

        // when
        BoardResponse response = boardService.update(board.getId(), req);

        // then
        verify(boardRepository).update(any(Board.class));
        assertThat(response.getTitle()).isEqualTo(req.getTitle());
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deleteBoard_Success() {
        // given
        given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));

        // when
        boardService.delete(board.getId());

        // then
        verify(boardRepository).delete(board.getId());
    }

}
