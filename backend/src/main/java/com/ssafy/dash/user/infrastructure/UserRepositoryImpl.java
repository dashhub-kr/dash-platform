package com.ssafy.dash.user.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.user.mapper.UserMapper;

@Repository
public class UserRepositoryImpl implements UserRepository {
    
    private final UserMapper userMapper;

    public UserRepositoryImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userMapper.selectById(id));
    }
}
