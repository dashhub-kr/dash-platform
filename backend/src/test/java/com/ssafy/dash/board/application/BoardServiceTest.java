package com.ssafy.dash.board.application;


import com.ssafy.dash.board.application.dto.command.BoardCreateCommand;
import com.ssafy.dash.board.application.dto.command.BoardUpdateCommand;
import com.ssafy.dash.board.application.dto.result.BoardResult;
import com.ssafy.dash.board.domain.Board;
import com.ssafy.dash.board.domain.BoardRepository;
import com.ssafy.dash.board.domain.exception.BoardNotFoundException;
import com.ssafy.dash.common.TestFixtures;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.user.domain.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardService 단위 테스트")
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserRepository userRepository;

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
    @DisplayName("사용자가 존재할 때 게시글을 생성한다")
    void createBoard_Success() {
        BoardCreateCommand command = TestFixtures.createBoardCreateCommand();
        given(userRepository.findById(command.userId())).willReturn(Optional.of(user));

        BoardResult response = boardService.create(command);

        verify(boardRepository).save(any(Board.class));
        assertThat(response.title()).isEqualTo(command.title());
        assertThat(response.authorName()).isEqualTo(user.getUsername());
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 게시글을 생성하면 UserNotFoundException이 발생한다")
    void createBoard_UserMissing_Throws() {
        BoardCreateCommand command = TestFixtures.createBoardCreateCommand();
        given(userRepository.findById(command.userId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.create(command))
                .isInstanceOf(UserNotFoundException.class);
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("ID로 게시글을 조회하면 작성자 정보를 포함해 반환한다")
    void findById_Success() {
        given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        BoardResult response = boardService.findById(board.getId());

        assertThat(response.id()).isEqualTo(board.getId());
        assertThat(response.authorName()).isEqualTo(user.getUsername());
    }

    @Test
    @DisplayName("없는 ID로 게시글을 조회하면 BoardNotFoundException이 발생한다")
    void findById_NotFound_Throws() {
        given(boardRepository.findById(board.getId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.findById(board.getId()))
                .isInstanceOf(BoardNotFoundException.class);
    }

    @Test
    @DisplayName("전체 게시글을 조회하면 리스트를 반환한다")
    void findAll_Success() {
        given(boardRepository.findAll()).willReturn(List.of(board));
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        List<BoardResult> responses = boardService.findAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).authorName()).isEqualTo(user.getUsername());
    }

    @Test
    @DisplayName("게시글을 수정하면 변경 내용이 반영된 결과를 반환한다")
    void updateBoard_Success() {
        BoardUpdateCommand command = TestFixtures.createBoardUpdateCommand();
        given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        BoardResult response = boardService.update(board.getId(), command);

        verify(boardRepository).update(any(Board.class));
        assertThat(response.title()).isEqualTo(command.title());
    }

    @Test
    @DisplayName("없는 게시글을 수정하면 BoardNotFoundException이 발생한다")
    void updateBoard_NotFound_Throws() {
        BoardUpdateCommand command = TestFixtures.createBoardUpdateCommand();
        given(boardRepository.findById(board.getId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.update(board.getId(), command))
                .isInstanceOf(BoardNotFoundException.class);
        verify(boardRepository, never()).update(any(Board.class));
    }

    @Test
    @DisplayName("게시글을 삭제하면 저장소에 삭제 명령을 전달한다")
    void deleteBoard_Success() {
        given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));

        boardService.delete(board.getId());

        verify(boardRepository).delete(board.getId());
    }

    @Test
    @DisplayName("없는 게시글을 삭제하면 BoardNotFoundException이 발생한다")
    void deleteBoard_NotFound_Throws() {
        given(boardRepository.findById(board.getId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.delete(board.getId()))
                .isInstanceOf(BoardNotFoundException.class);
        verify(boardRepository, never()).delete(anyLong());
    }

}
