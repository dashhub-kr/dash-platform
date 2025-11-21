package com.ssafy.dash.user.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dash.user.User;
import com.ssafy.dash.user.mapper.UserMapper;
import com.ssafy.dash.user.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public User create(User user) {
        userMapper.insertUser(user);
        return userMapper.selectUserById(user.getId());
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectUserById(id);
    }

    @Override
    public User getByGithubId(Long githubId) {
        return userMapper.selectUserByGithubId(githubId);
    }

    @Override
    public List<User> listAll() {
        return userMapper.selectAllUsers();
    }

    @Override
    @Transactional
    public User update(Long id, User user) {
        user.setId(id);
        userMapper.updateUser(user);
        return userMapper.selectUserById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userMapper.deleteUser(id);
    }
    
}
