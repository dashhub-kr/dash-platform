package com.ssafy.dash.algorithm.application;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dash.algorithm.application.dto.AlgorithmRecordCreateRequest;
import com.ssafy.dash.algorithm.application.dto.AlgorithmRecordResponse;
import com.ssafy.dash.algorithm.application.dto.AlgorithmRecordUpdateRequest;
import com.ssafy.dash.algorithm.domain.AlgorithmRecord;
import com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository;
import com.ssafy.dash.algorithm.domain.exception.AlgorithmRecordNotFoundException;
import com.ssafy.dash.user.domain.User;
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
    public AlgorithmRecordResponse create(Long userId, AlgorithmRecordCreateRequest req) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        String code = "";
        if (req.getFile() != null && !req.getFile().isEmpty()) {
            code = new String(req.getFile().getBytes(), StandardCharsets.UTF_8);
        }

        AlgorithmRecord record = new AlgorithmRecord();
        record.setUserId(userId);
        record.setProblemNumber(req.getProblemNumber());
        record.setTitle(req.getTitle());
        record.setCode(code);
        record.setLanguage(req.getLanguage());
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());

        algorithmRecordRepository.save(record);

        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public AlgorithmRecordResponse findById(Long id) {
        AlgorithmRecord record = algorithmRecordRepository.findById(id)
                .orElseThrow(() -> new AlgorithmRecordNotFoundException(id));

        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public List<AlgorithmRecordResponse> findAll() {
        return algorithmRecordRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlgorithmRecordResponse> findByUserId(Long userId) {
        return algorithmRecordRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AlgorithmRecordResponse update(Long id, AlgorithmRecordUpdateRequest req) throws IOException {
        AlgorithmRecord record = algorithmRecordRepository.findById(id)
                .orElseThrow(() -> new AlgorithmRecordNotFoundException(id));

        if (req.getProblemNumber() != null) record.setProblemNumber(req.getProblemNumber());
        if (req.getTitle() != null) record.setTitle(req.getTitle());
        if (req.getLanguage() != null) record.setLanguage(req.getLanguage());
        
        if (req.getFile() != null && !req.getFile().isEmpty()) {
            String code = new String(req.getFile().getBytes(), StandardCharsets.UTF_8);
            record.setCode(code);
        }
        
        record.setUpdatedAt(LocalDateTime.now());

        algorithmRecordRepository.update(record);

        return toResponse(record);
    }

    @Transactional
    public void delete(Long id) {
        boolean deleted = algorithmRecordRepository.delete(id);
        if (!deleted) {
            throw new AlgorithmRecordNotFoundException(id);
        }
    }

    private AlgorithmRecordResponse toResponse(AlgorithmRecord record) {
        return new AlgorithmRecordResponse(
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
