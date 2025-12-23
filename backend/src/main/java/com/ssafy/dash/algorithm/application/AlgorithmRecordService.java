package com.ssafy.dash.algorithm.application;

import com.ssafy.dash.algorithm.application.dto.command.AlgorithmRecordCreateCommand;
import com.ssafy.dash.algorithm.application.dto.command.AlgorithmRecordUpdateCommand;
import com.ssafy.dash.algorithm.application.dto.result.AlgorithmRecordResult;
import com.ssafy.dash.algorithm.domain.AlgorithmRecord;
import com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository;
import com.ssafy.dash.algorithm.domain.exception.AlgorithmRecordNotFoundException;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.user.domain.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import com.ssafy.dash.dashboard.application.dto.result.HeatmapItem;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AlgorithmRecordService {

    private final AlgorithmRecordRepository algorithmRecordRepository;
    private final UserRepository userRepository;
    private final com.ssafy.dash.study.application.StudyMissionService studyMissionService;

    public AlgorithmRecordService(AlgorithmRecordRepository algorithmRecordRepository, UserRepository userRepository,
            com.ssafy.dash.study.application.StudyMissionService studyMissionService) {
        this.algorithmRecordRepository = algorithmRecordRepository;
        this.userRepository = userRepository;
        this.studyMissionService = studyMissionService;
    }

    @Transactional
    public AlgorithmRecordResult create(AlgorithmRecordCreateCommand command) {
        userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        LocalDateTime now = LocalDateTime.now();
        AlgorithmRecord record = AlgorithmRecord.create(
                command.userId(),
                userRepository.findById(command.userId())
                        .map(com.ssafy.dash.user.domain.User::getStudyId)
                        .orElse(null),
                command.problemNumber(),
                command.title(),
                command.language(),
                command.code(),
                now);

        algorithmRecordRepository.save(record);

        // 스터디 미션 완료 체크
        try {
            int problemId = Integer.parseInt(command.problemNumber());
            studyMissionService.checkAndMarkCompleted(command.userId(), problemId);
        } catch (NumberFormatException e) {
            // ignore non-integer problem numbers
        }

        return AlgorithmRecordResult.from(record);
    }

    @Transactional(readOnly = true)
    public AlgorithmRecordResult findById(Long id) {
        AlgorithmRecord record = algorithmRecordRepository.findById(id)
                .orElseThrow(() -> new AlgorithmRecordNotFoundException(id));

        return AlgorithmRecordResult.from(record);
    }

    @Transactional(readOnly = true)
    public List<AlgorithmRecordResult> findAll() {

        return algorithmRecordRepository.findAll().stream()
                .map(AlgorithmRecordResult::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlgorithmRecordResult> findByUserId(Long userId) {

        return algorithmRecordRepository.findByUserId(userId).stream()
                .map(AlgorithmRecordResult::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlgorithmRecordResult> findByStudyId(Long studyId) {

        return algorithmRecordRepository.findByStudyId(studyId).stream()
                .map(AlgorithmRecordResult::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public AlgorithmRecordResult update(Long id, AlgorithmRecordUpdateCommand command) {
        AlgorithmRecord record = algorithmRecordRepository.findById(id)
                .orElseThrow(() -> new AlgorithmRecordNotFoundException(id));

        LocalDateTime now = LocalDateTime.now();
        record.applyUpdate(command.problemNumber(), command.title(), command.language(), command.code(), now);

        algorithmRecordRepository.update(record);

        return AlgorithmRecordResult.from(record);
    }

    @Transactional
    public void delete(Long id) {
        boolean deleted = algorithmRecordRepository.delete(id);
        if (!deleted) {
            throw new AlgorithmRecordNotFoundException(id);
        }
    }

    @Transactional(readOnly = true)
    public List<HeatmapItem> getHeatmap(Long userId, Long studyId) {
        List<AlgorithmRecord> records;
        if (studyId != null) {
            records = algorithmRecordRepository.findByStudyId(studyId);
        } else {
            records = algorithmRecordRepository.findByUserId(userId);
        }

        // Group by Date (YYYY-MM-DD)
        Map<String, List<AlgorithmRecord>> groupedByDate = records.stream()
                .collect(Collectors.groupingBy(record -> record.getCommittedAt().toLocalDate().toString()));

        return groupedByDate.entrySet().stream()
                .map(entry -> {
                    String date = entry.getKey();
                    List<AlgorithmRecord> dailyRecords = entry.getValue();
                    Long count = (long) dailyRecords.size();

                    List<String> contributors = dailyRecords.stream()
                            .map(AlgorithmRecord::getUsername)
                            .filter(java.util.Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList());

                    return HeatmapItem.of(date, count, contributors);
                })
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());
    }

}
