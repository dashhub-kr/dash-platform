package com.ssafy.dash.user.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.user.infrastructure.mapper.UserMapper;

@Repository
public class UserRepositoryImpl implements UserRepository {
    
    private final UserMapper userMapper;

    public UserRepositoryImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void save(User user) {
        userMapper.insert(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userMapper.selectById(id));
    }

    @Override
    public List<User> findAll() {
        return userMapper.selectAll();
    }

    @Override
    public Optional<User> findByProviderAndProviderId(String provider, String providerId) {
        return Optional.ofNullable(userMapper.selectByProviderAndProviderId(provider, providerId));
    }

    @Override
    public void update(User user) {
        userMapper.update(user);
    }

    @Override
    public boolean delete(Long id) {
        return userMapper.delete(id) > 0;
    }
    
}
