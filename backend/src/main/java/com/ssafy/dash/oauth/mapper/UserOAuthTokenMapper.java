package com.ssafy.dash.oauth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.dash.oauth.domain.UserOAuthToken;

@Mapper
public interface UserOAuthTokenMapper {

    UserOAuthToken selectByUserId(@Param("userId") Long userId);

    void insert(UserOAuthToken token);

    void update(UserOAuthToken token);

}
