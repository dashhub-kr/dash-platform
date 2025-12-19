package com.ssafy.dash.analytics.domain;

import java.util.List;

public interface UserClassStatRepository {
    void save(UserClassStat stat);

    List<UserClassStat> findByUserId(Long userId);

    UserClassStat findByUserIdAndClass(Long userId, Integer classNumber);

    void deleteByUserId(Long userId);
}
