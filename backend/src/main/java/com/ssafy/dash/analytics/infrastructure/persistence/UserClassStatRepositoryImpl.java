package com.ssafy.dash.analytics.infrastructure.persistence;

import com.ssafy.dash.analytics.domain.UserClassStat;
import com.ssafy.dash.analytics.domain.UserClassStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserClassStatRepositoryImpl implements UserClassStatRepository {

    private final UserClassStatMapper classStatMapper;

    @Override
    public void save(UserClassStat stat) {
        classStatMapper.upsert(stat);
    }

    @Override
    public List<UserClassStat> findByUserId(Long userId) {
        return classStatMapper.findByUserId(userId);
    }

    @Override
    public UserClassStat findByUserIdAndClass(Long userId, Integer classNumber) {
        return classStatMapper.findByUserIdAndClass(userId, classNumber);
    }

    @Override
    public void deleteByUserId(Long userId) {
        classStatMapper.deleteByUserId(userId);
    }
}
