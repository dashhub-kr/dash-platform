package com.ssafy.dash.analytics.infrastructure.persistence;

import com.ssafy.dash.analytics.domain.UserStatsSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface UserStatsSnapshotMapper {

    void insert(UserStatsSnapshot snapshot);

    Optional<UserStatsSnapshot> findByUserIdAndDate(
            @Param("userId") Long userId,
            @Param("snapshotDate") LocalDate snapshotDate);

    Optional<UserStatsSnapshot> findLatestByUserId(@Param("userId") Long userId);

    List<UserStatsSnapshot> findByUserIdBetweenDates(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    void deleteOlderThan(@Param("date") LocalDate date);
}
