package com.ssafy.dash.analytics.infrastructure.persistence;

import com.ssafy.dash.analytics.application.dto.FamilyScoreDto;
import com.ssafy.dash.analytics.application.dto.TagCoverageDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AnalyticsMapper {
    /**
     * Calculate raw scores per family for a user.
     */
    List<FamilyScoreDto> findFamilyScoresByUserId(@Param("userId") Long userId);

    /**
     * Calculate core tag coverage for a specific family.
     */
    TagCoverageDto findCoreCoverageByUserId(@Param("userId") Long userId);
}
