package com.ssafy.dash.algorithm.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dash.algorithm.application.dto.AlgorithmRecordCreateCommand;
import com.ssafy.dash.algorithm.application.dto.AlgorithmRecordResult;
import com.ssafy.dash.algorithm.application.dto.AlgorithmRecordUpdateCommand;
import com.ssafy.dash.algorithm.domain.AlgorithmRecord;
import com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository;
import com.ssafy.dash.algorithm.domain.exception.AlgorithmRecordNotFoundException;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.user.domain.exception.UserNotFoundException;

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
        userRepository.findById(command.getUserId())
            .orElseThrow(() -> new UserNotFoundException(command.getUserId()));

        AlgorithmRecord record = new AlgorithmRecord();
        record.setUserId(command.getUserId());
        record.setProblemNumber(command.getProblemNumber());
        record.setTitle(command.getTitle());
        record.setCode(command.getCode());
        record.setLanguage(command.getLanguage());
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());

        algorithmRecordRepository.save(record);

        return toResult(record);
    }

    @Transactional(readOnly = true)
    public AlgorithmRecordResult findById(Long id) {
        AlgorithmRecord record = algorithmRecordRepository.findById(id)
                .orElseThrow(() -> new AlgorithmRecordNotFoundException(id));

        return toResult(record);
    }

    @Transactional(readOnly = true)
    public List<AlgorithmRecordResult> findAll() {

        return algorithmRecordRepository.findAll().stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlgorithmRecordResult> findByUserId(Long userId) {

        return algorithmRecordRepository.findByUserId(userId).stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    @Transactional
    public AlgorithmRecordResult update(Long id, AlgorithmRecordUpdateCommand command) {
        AlgorithmRecord record = algorithmRecordRepository.findById(id)
                .orElseThrow(() -> new AlgorithmRecordNotFoundException(id));

        if (command.getProblemNumber() != null) {
            record.setProblemNumber(command.getProblemNumber());
        }
        if (command.getTitle() != null) {
            record.setTitle(command.getTitle());
        }
        if (command.getLanguage() != null) {
            record.setLanguage(command.getLanguage());
        }
        if (command.getCode() != null) {
            record.setCode(command.getCode());
        }
        
        record.setUpdatedAt(LocalDateTime.now());

        algorithmRecordRepository.update(record);

        return toResult(record);
    }

    @Transactional
    public void delete(Long id) {
        boolean deleted = algorithmRecordRepository.delete(id);
        if (!deleted) {

            throw new AlgorithmRecordNotFoundException(id);
        }
    }

    private AlgorithmRecordResult toResult(AlgorithmRecord record) {
        
        return new AlgorithmRecordResult(
            record.getId(),
            record.getUserId(),
            record.getProblemNumber(),
            record.getTitle(),
            record.getCode(),
            record.getLanguage(),
            record.getCreatedAt(),
            record.getUpdatedAt()
        );
    }
    
}
