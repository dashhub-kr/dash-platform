package com.ssafy.dash.dashboard.application;

import com.ssafy.dash.algorithm.domain.AlgorithmRecord;
import com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository;
import com.ssafy.dash.dashboard.application.dto.response.HeatmapItem;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AlgorithmRecordRepository algorithmRecordRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<com.ssafy.dash.algorithm.application.dto.result.AlgorithmRecordResult> getAlgorithmRecords(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.ssafy.dash.user.domain.exception.UserNotFoundException(userId));

        List<AlgorithmRecord> records;
        if (user.getStudyId() != null) {
            records = algorithmRecordRepository.findByStudyId(user.getStudyId());
        } else {
            records = algorithmRecordRepository.findByUserId(userId);
        }
        
        return records.stream()
                .map(com.ssafy.dash.algorithm.application.dto.result.AlgorithmRecordResult::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<com.ssafy.dash.algorithm.application.dto.result.AlgorithmRecordResult> getAlgorithmRecordsByStudyId(Long studyId) {
        return algorithmRecordRepository.findByStudyId(studyId).stream()
                .map(com.ssafy.dash.algorithm.application.dto.result.AlgorithmRecordResult::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HeatmapItem> getHeatmap(Long userId, Long studyId) {
        List<com.ssafy.dash.dashboard.application.dto.response.HeatmapRawData> rawData;
        if (studyId != null) {
            rawData = algorithmRecordRepository.findHeatmapDataByStudyId(studyId);
        } else {
            rawData = algorithmRecordRepository.findHeatmapDataByUserId(userId);
        }

        // 날짜별 그룹화
        Map<String, List<com.ssafy.dash.dashboard.application.dto.response.HeatmapRawData>> groupedByDate = rawData.stream()
                .collect(Collectors.groupingBy(com.ssafy.dash.dashboard.application.dto.response.HeatmapRawData::getDate));

        return groupedByDate.entrySet().stream()
                .map(entry -> {
                    String date = entry.getKey();
                    List<com.ssafy.dash.dashboard.application.dto.response.HeatmapRawData> dailyLogs = entry.getValue();
                    
                    // 해당 날짜의 총 제출 수 계산
                    long totalCount = dailyLogs.stream()
                            .mapToLong(com.ssafy.dash.dashboard.application.dto.response.HeatmapRawData::getDailyCount)
                            .sum();

                    // 기여자 정보 매핑
                    List<HeatmapItem.ContributorInfo> contributors = dailyLogs.stream()
                            .map(log -> HeatmapItem.ContributorInfo.of(
                                    log.getUserId(),
                                    log.getUsername(),
                                    log.getAvatarUrl(),
                                    log.getDailyCount()
                            ))
                            .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                            .collect(Collectors.toList());

                    return HeatmapItem.of(date, totalCount, contributors);
                })
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());
    }
}
