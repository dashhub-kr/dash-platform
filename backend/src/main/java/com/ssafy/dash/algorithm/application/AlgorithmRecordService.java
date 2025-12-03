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
import java.util.stream.Collectors;

@Service
public class AlgorithmRecordService {

    private final AlgorithmRecordRepository algorithmRecordRepository;
    private final UserRepository userRepository;

    public AlgorithmRecordService(AlgorithmRecordRepository algorithmRecordRepository, UserRepository userRepository) {
        this.algorithmRecordRepository = algorithmRecordRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AlgorithmRecordResult create(AlgorithmRecordCreateCommand command) {
        userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        LocalDateTime now = LocalDateTime.now();
        AlgorithmRecord record = AlgorithmRecord.create(command.userId(), command.problemNumber(), command.title(), command.language(), command.code(), now);

        algorithmRecordRepository.save(record);

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

}
