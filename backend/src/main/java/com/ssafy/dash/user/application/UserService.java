package com.ssafy.dash.user.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dash.oauth.domain.AuthFlowType;
import com.ssafy.dash.oauth.application.dto.OAuthLoginResult;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.user.application.dto.UserCreateCommand;
import com.ssafy.dash.user.application.dto.UserResult;
import com.ssafy.dash.user.application.dto.UserUpdateCommand;
import com.ssafy.dash.user.domain.exception.UserNotFoundException;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResult create(UserCreateCommand command) {
        User u = new User();
        u.setUsername(command.getUsername());
        u.setEmail(command.getEmail());
        u.setCreatedAt(LocalDateTime.now());
        userRepository.save(u);

        return toResult(u);
    }

    @Transactional(readOnly = true)
    public UserResult findById(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return toResult(u);
    }

    @Transactional(readOnly = true)
    public List<UserResult> findAll() {
        
        return userRepository.findAll().stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResult update(Long id, UserUpdateCommand command) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        u.setUsername(command.getUsername());
        u.setEmail(command.getEmail());
        userRepository.update(u);

        return toResult(u);
    }

    @Transactional
    public void delete(Long id) {
        boolean deleted = userRepository.delete(id);
        if (!deleted) throw new UserNotFoundException(id);
    }

    @Transactional
    public OAuthLoginResult createOrUpdateOAuthUser(String provider, String providerId, String login, String email, String avatarUrl) {
        User user = userRepository.findByProviderAndProviderId(provider, providerId).orElse(null);
        AuthFlowType flowType;

        if (user == null) {
            user = new User();
            user.setUsername(login);
            user.setEmail(email);
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setAvatarUrl(avatarUrl);
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
            flowType = AuthFlowType.SIGN_UP;
        } else {
            user.setUsername(login);
            user.setEmail(email);
            user.setAvatarUrl(avatarUrl);
            userRepository.update(user);
            flowType = AuthFlowType.LOGIN;
        }

        return new OAuthLoginResult(user, flowType);
    }

    @Transactional(readOnly = true)
    public UserResult findByProviderAndId(String provider, String providerId) {
        User u = userRepository.findByProviderAndProviderId(provider, providerId).orElse(null);
        if (u == null) return null;
        
        return toResult(u);
    }

    private UserResult toResult(User u) {

        return new UserResult(u.getId(), u.getUsername(), u.getEmail(), u.getCreatedAt(), u.getProvider(), u.getProviderId(), u.getAvatarUrl());
    }

}
