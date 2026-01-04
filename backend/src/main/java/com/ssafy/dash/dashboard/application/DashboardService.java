package com.ssafy.dash.dashboard.application;

import com.ssafy.dash.algorithm.domain.AlgorithmRecord;
import com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository;
import com.ssafy.dash.dashboard.application.dto.response.HeatmapItem;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
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
        List<AlgorithmRecord> records;
        if (studyId != null) {
            records = algorithmRecordRepository.findByStudyId(studyId);
        } else {
            records = algorithmRecordRepository.findByUserId(userId);
        }

        // Get user info map for avatarUrl
        Map<Long, User> userMap = new HashMap<>();
        records.stream()
                .map(AlgorithmRecord::getUserId)
                .distinct()
                .forEach(uid -> userRepository.findById(uid).ifPresent(u -> userMap.put(uid, u)));

        // Group by Date (YYYY-MM-DD)
        Map<String, List<AlgorithmRecord>> groupedByDate = records.stream()
                .collect(Collectors.groupingBy(record -> record.getCommittedAt().toLocalDate().toString()));

        return groupedByDate.entrySet().stream()
                .map(entry -> {
                    String date = entry.getKey();
                    List<AlgorithmRecord> dailyRecords = entry.getValue();
                    Long count = (long) dailyRecords.size();

                    // Group by userId and count submissions per user
                    List<HeatmapItem.ContributorInfo> contributors = dailyRecords.stream()
                            .collect(Collectors.groupingBy(AlgorithmRecord::getUserId, Collectors.counting()))
                            .entrySet().stream()
                            .map(e -> {
                                Long uid = e.getKey();
                                Long submitCount = e.getValue();
                                User user = userMap.get(uid);
                                String username = user != null ? user.getUsername() : "Unknown";
                                String avatarUrl = user != null ? user.getAvatarUrl() : null;
                                return HeatmapItem.ContributorInfo.of(uid, username, avatarUrl, submitCount);
                            })
                            .sorted((a, b) -> Long.compare(b.getCount(), a.getCount())) // Sort by count desc
                            .collect(Collectors.toList());

                    return HeatmapItem.of(date, count, contributors);
                })
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());
    }
}
