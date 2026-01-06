package com.ssafy.dash.user.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.dash.user.domain.User;

@Mapper
public interface UserMapper {

    int insert(User user);

    User selectById(Long id);

    List<User> selectAll();

    List<User> selectByKeyword(String keyword);

    User selectByProviderAndProviderId(String provider, String providerId);

    int update(User user);

    int delete(Long id);

    List<User> selectByStudyId(Long studyId);

    List<User> selectUsersForAnonymization(java.time.LocalDateTime threshold);

    int anonymize(Long id);

}
