package com.ssafy.dash.user.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.dash.user.User;

@Mapper
public interface UserMapper {

    int insertUser(User user);

    User selectUserById(@Param("id") Long id);

    User selectUserByGithubId(@Param("githubId") Long githubId);

    List<User> selectAllUsers();

    int updateUser(User user);

    int deleteUser(@Param("id") Long id);
    
}
