package com.ssafy.dash.analytics.infrastructure.persistence;

import com.ssafy.dash.analytics.domain.UserTagStat;
import com.ssafy.dash.analytics.domain.UserFamilyStat;
import com.ssafy.dash.analytics.domain.UserTagStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserTagStatRepositoryImpl implements UserTagStatRepository {

    private final UserTagStatMapper tagStatMapper;

    @Override
    public void save(UserTagStat stat) {
        tagStatMapper.upsert(stat);
    }

    @Override
    public List<UserTagStat> findByUserId(Long userId) {
        return tagStatMapper.findByUserId(userId);
    }

    @Override
    public List<UserFamilyStat> findFamilyStatsByUserId(Long userId) {
        return tagStatMapper.findFamilyStatsByUserId(userId);
    }

    @Override
    public void deleteByUserId(Long userId) {
        tagStatMapper.deleteByUserId(userId);
    }
}
