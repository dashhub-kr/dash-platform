package com.ssafy.dash.comment.application;

import com.ssafy.dash.comment.application.dto.command.CommentCreateCommand;
import com.ssafy.dash.comment.application.dto.command.CommentUpdateCommand;
import com.ssafy.dash.comment.application.dto.result.CommentResult;
import com.ssafy.dash.comment.domain.Comment;
import com.ssafy.dash.comment.domain.CommentRepository;
import com.ssafy.dash.comment.domain.exception.CommentNotFoundException;
import com.ssafy.dash.common.fixtures.CommentFixtures;
import com.ssafy.dash.common.fixtures.UserFixtures;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 단위 테스트")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private User user;
    private Comment comment;

    @BeforeEach
    void setUp() {
        user = UserFixtures.createUser();
        comment = CommentFixtures.createComment(1L, user);
    }

    @Nested
    @DisplayName("create 메서드")
    class CreateTests {

        @Test
        @DisplayName("사용자가 존재하면 댓글을 생성한다")
        void create_WhenUserExists_ShouldCreateComment() {
            CommentCreateCommand command = CommentFixtures.createCommentCreateCommand(1L, user.getId());
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

            CommentResult result = commentService.create(command);

            assertThat(result.content()).isEqualTo(command.content());
            assertThat(result.authorName()).isEqualTo(user.getUsername());
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("대댓글 생성 시 부모 댓글이 최상위이면 성공한다")
        void create_Reply_WhenParentIsTopLevel_ShouldSucceed() {
            Comment parent = CommentFixtures.createComment(1L, user);
            CommentCreateCommand command = CommentFixtures.createReplyCreateCommand(1L, user.getId(), parent.getId());

            given(commentRepository.findById(parent.getId())).willReturn(Optional.of(parent));
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

            CommentResult result = commentService.create(command);

            assertThat(result.parentId()).isEqualTo(parent.getId());
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("대댓글의 대댓글 시도 시 IllegalArgumentException이 발생한다")
        void create_ReplyToReply_ShouldThrowException() {
            Comment parent = CommentFixtures.createReplyComment(1L, user, 99L);
            CommentCreateCommand command = CommentFixtures.createReplyCreateCommand(1L, user.getId(), parent.getId());

            given(commentRepository.findById(parent.getId())).willReturn(Optional.of(parent));

            assertThatThrownBy(() -> commentService.create(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("one level");

            verify(commentRepository, never()).save(any(Comment.class));
        }
    }

    @Nested
    @DisplayName("findById 메서드")
    class FindByIdTests {

        @Test
        @DisplayName("존재하는 ID로 조회하면 결과를 반환한다")
        void findById_WhenExists_ShouldReturnResult() {
            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
            given(userRepository.findById(comment.getUserId())).willReturn(Optional.of(user));

            CommentResult result = commentService.findById(comment.getId());

            assertThat(result.id()).isEqualTo(comment.getId());
            assertThat(result.content()).isEqualTo(comment.getContent());
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 예외가 발생한다")
        void findById_WhenNotExists_ShouldThrowException() {
            given(commentRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.findById(999L))
                    .isInstanceOf(CommentNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findByBoardId 메서드")
    class FindByBoardIdTests {

        @Test
        @DisplayName("게시글의 댓글 목록을 트리 구조로 반환한다")
        void findByBoardId_ShouldReturnTreeStructure() {
            Comment parent = CommentFixtures.createComment(1L, user);
            Comment reply = CommentFixtures.createReplyComment(1L, user, parent.getId());
            reply.setAuthorName(user.getUsername());
            parent.setAuthorName(user.getUsername());

            given(commentRepository.findByBoardId(1L)).willReturn(List.of(parent, reply));

            List<CommentResult> results = commentService.findByBoardId(1L, null);

            assertThat(results).hasSize(1); // 최상위 댓글만
            assertThat(results.get(0).replies()).hasSize(1); // 대댓글은 replies에
        }

        @Test
        @DisplayName("라인 번호로 필터링하면 해당 라인 댓글만 반환한다")
        void findByBoardId_WithLineNumber_ShouldFilterByLine() {
            Comment lineComment = CommentFixtures.createLineComment(1L, user, 42);
            lineComment.setAuthorName(user.getUsername());

            given(commentRepository.findByBoardIdAndLineNumber(1L, 42)).willReturn(List.of(lineComment));

            List<CommentResult> results = commentService.findByBoardId(1L, 42);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).lineNumber()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("update 메서드")
    class UpdateTests {

        @Test
        @DisplayName("존재하는 댓글을 수정하면 변경된 결과를 반환한다")
        void update_WhenExists_ShouldUpdateAndReturn() {
            CommentUpdateCommand command = CommentFixtures.createCommentUpdateCommand();
            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
            given(userRepository.findById(comment.getUserId())).willReturn(Optional.of(user));

            CommentResult result = commentService.update(comment.getId(), command);

            assertThat(result.content()).isEqualTo(command.content());
            verify(commentRepository).update(any(Comment.class));
        }

        @Test
        @DisplayName("존재하지 않는 댓글을 수정하면 예외가 발생한다")
        void update_WhenNotExists_ShouldThrowException() {
            CommentUpdateCommand command = CommentFixtures.createCommentUpdateCommand();
            given(commentRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.update(999L, command))
                    .isInstanceOf(CommentNotFoundException.class);

            verify(commentRepository, never()).update(any(Comment.class));
        }
    }

    @Nested
    @DisplayName("delete 메서드")
    class DeleteTests {

        @Test
        @DisplayName("존재하는 댓글을 삭제할 수 있다")
        void delete_WhenExists_ShouldDelete() {
            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));

            commentService.delete(comment.getId());

            verify(commentRepository).delete(comment.getId());
        }

        @Test
        @DisplayName("존재하지 않는 댓글을 삭제하면 예외가 발생한다")
        void delete_WhenNotExists_ShouldThrowException() {
            given(commentRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.delete(999L))
                    .isInstanceOf(CommentNotFoundException.class);

            verify(commentRepository, never()).delete(999L);
        }
    }

}
