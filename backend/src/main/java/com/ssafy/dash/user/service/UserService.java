package com.ssafy.dash.user.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.dto.UserCreateRequest;
import com.ssafy.dash.user.dto.UserResponse;
import com.ssafy.dash.user.dto.UserUpdateRequest;
import com.ssafy.dash.user.exception.UserNotFoundException;
import com.ssafy.dash.user.mapper.UserMapper;

@Service
public class UserService {

    private final UserMapper mapper;

    public UserService(UserMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional
    public UserResponse create(UserCreateRequest req) {
        User u = new User();
        u.setUsername(req.getUsername());
        u.setEmail(req.getEmail());
        u.setCreatedAt(LocalDateTime.now());
        mapper.insert(u);

        return toResponse(u);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User u = mapper.selectById(id);
        if (u == null) throw new UserNotFoundException(id);

        return toResponse(u);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {

        return mapper.selectAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest req) {
        User u = mapper.selectById(id);
        if (u == null) throw new UserNotFoundException(id);
        u.setUsername(req.getUsername());
        u.setEmail(req.getEmail());
        mapper.update(u);

        return toResponse(u);
    }

    @Transactional
    public void delete(Long id) {
        int deleted = mapper.delete(id);
        if (deleted == 0) throw new UserNotFoundException(id);
    }

    @Transactional
    public User createOrUpdateOAuthUser(String provider, String providerId, String login, String email, String avatarUrl) {
        User user = mapper.selectByProviderAndProviderId(provider, providerId);
        
        if (user == null) {
            user = new User();
            user.setUsername(login);
            user.setEmail(email);
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setAvatarUrl(avatarUrl);
            user.setCreatedAt(LocalDateTime.now());
            mapper.insert(user);
        } else {
            user.setUsername(login);
            user.setEmail(email);
            user.setAvatarUrl(avatarUrl);
            mapper.update(user);
        }
        
        return user;
    }

    @Transactional(readOnly = true)
    public UserResponse findByProviderAndId(String provider, String providerId) {
        User u = mapper.selectByProviderAndProviderId(provider, providerId);
        if (u == null) return null;
        
        return toResponse(u);
    }

    private UserResponse toResponse(User u) {
        
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getCreatedAt(), u.getProvider(), u.getProviderId(), u.getAvatarUrl());
    }

}
