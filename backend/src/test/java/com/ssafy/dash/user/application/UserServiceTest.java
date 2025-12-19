package com.ssafy.dash.user.application;

import com.ssafy.dash.common.TestFixtures;
import com.ssafy.dash.user.application.dto.command.UserCreateCommand;
import com.ssafy.dash.user.application.dto.command.UserUpdateCommand;
import com.ssafy.dash.user.application.dto.result.UserResult;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.user.domain.exception.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("유저를 생성하면 저장 후 결과 DTO를 반환한다")
    void createUser() {
        UserCreateCommand command = TestFixtures.createUserCreateCommand();

        UserResult result = userService.create(command);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo(command.username());
        assertThat(saved.getEmail()).isEqualTo(command.email());
        assertThat(result.username()).isEqualTo(command.username());
    }

    @Test
    @DisplayName("ID로 유저를 조회하면 UserResult로 변환한다")
    void findByIdReturnsResult() {
        User user = TestFixtures.createUser();
        given(userRepository.findById(TestFixtures.TEST_USER_ID)).willReturn(Optional.of(user));

        UserResult result = userService.findById(TestFixtures.TEST_USER_ID);

        assertThat(result.id()).isEqualTo(user.getId());
        verify(userRepository).findById(TestFixtures.TEST_USER_ID);
    }

    @Test
    @DisplayName("조회 시 ID가 존재하지 않으면 UserNotFoundException이 발생한다")
    void findByIdThrowsWhenMissing() {
        given(userRepository.findById(TestFixtures.TEST_USER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(TestFixtures.TEST_USER_ID))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("전체 유저를 조회하면 UserResult 리스트를 반환한다")
    void findAllReturnsResults() {
        given(userRepository.findAll()).willReturn(List.of(TestFixtures.createUser()));

        List<UserResult> results = userService.findAll();

        assertThat(results).hasSize(1);
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("유저 정보를 수정하면 엔티티와 결과가 함께 갱신된다")
    void updateUser() {
        User user = TestFixtures.createUser();
        given(userRepository.findById(TestFixtures.TEST_USER_ID)).willReturn(Optional.of(user));
        UserUpdateCommand command = TestFixtures.createUserUpdateCommand();

        UserResult result = userService.update(TestFixtures.TEST_USER_ID, command);

        assertThat(user.getUsername()).isEqualTo(command.username());
        assertThat(result.username()).isEqualTo(command.username());
        verify(userRepository).update(user);
    }

    @Test
    @DisplayName("유저 삭제에 성공하면 예외 없이 끝난다")
    void deleteUser() {
        given(userRepository.delete(TestFixtures.TEST_USER_ID)).willReturn(true);

        userService.delete(TestFixtures.TEST_USER_ID);

        verify(userRepository).delete(TestFixtures.TEST_USER_ID);
    }

    @Test
    @DisplayName("삭제 대상이 없으면 UserNotFoundException이 발생한다")
    void deleteUserThrowsWhenMissing() {
        given(userRepository.delete(TestFixtures.TEST_USER_ID)).willReturn(false);

        assertThatThrownBy(() -> userService.delete(TestFixtures.TEST_USER_ID))
                .isInstanceOf(UserNotFoundException.class);
    }
    
    @Mock
    private com.ssafy.dash.onboarding.domain.OnboardingRepository onboardingRepository;

    @Test
    @DisplayName("ID로 조회 시 온보딩 정보가 있으면 함께 반환한다")
    void findByIdReturnsResultWithOnboarding() {
        User user = TestFixtures.createUser();
        com.ssafy.dash.onboarding.domain.Onboarding onboarding = TestFixtures.createOnboarding(user.getId(), true);
        
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(onboardingRepository.findByUserId(user.getId())).willReturn(Optional.of(onboarding));

        UserResult result = userService.findById(user.getId());

        assertThat(result.id()).isEqualTo(user.getId());
        assertThat(result.repositoryName()).isEqualTo(onboarding.getRepositoryName());
        assertThat(result.webhookConfigured()).isTrue();
    }
    
}
