package com.ssafy.dash.user.service;

import java.util.List;

import com.ssafy.dash.user.User;

public interface UserService {

    User create(User user);

    User getById(Long id);

    User getByGithubId(Long githubId);

    List<User> listAll();

    User update(Long id, User user);

    void delete(Long id);
    
}
