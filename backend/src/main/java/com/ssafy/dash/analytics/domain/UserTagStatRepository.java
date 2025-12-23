package com.ssafy.dash.analytics.domain;

import java.util.List;

public interface UserTagStatRepository {
    void save(UserTagStat stat);

    List<UserTagStat> findByUserId(Long userId);

    List<UserFamilyStat> findFamilyStatsByUserId(Long userId);

    void deleteByUserId(Long userId);
}
