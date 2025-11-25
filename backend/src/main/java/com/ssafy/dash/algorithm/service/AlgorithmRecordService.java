package com.ssafy.dash.algorithm.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ssafy.dash.algorithm.domain.AlgorithmRecord;
import com.ssafy.dash.algorithm.dto.AlgorithmRecordCreateRequest;
import com.ssafy.dash.algorithm.dto.AlgorithmRecordResponse;
import com.ssafy.dash.algorithm.dto.AlgorithmRecordUpdateRequest;
import com.ssafy.dash.algorithm.exception.AlgorithmRecordNotFoundException;
import com.ssafy.dash.algorithm.mapper.AlgorithmRecordMapper;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.exception.UserNotFoundException;
import com.ssafy.dash.user.mapper.UserMapper;

@Service
public class AlgorithmRecordService {

    private final AlgorithmRecordMapper algorithmRecordMapper;
    private final UserMapper userMapper;

    public AlgorithmRecordService(AlgorithmRecordMapper algorithmRecordMapper, UserMapper userMapper) {
        this.algorithmRecordMapper = algorithmRecordMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public AlgorithmRecordResponse create(Long userId, AlgorithmRecordCreateRequest req) throws IOException {
        User user = userMapper.selectById(userId);
        if (user == null) {

            throw new UserNotFoundException(userId);
        }

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

        algorithmRecordMapper.insert(record);

        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public AlgorithmRecordResponse findById(Long id) {
        AlgorithmRecord record = algorithmRecordMapper.selectById(id);
        if (record == null) {
            
            throw new AlgorithmRecordNotFoundException(id);
        }

        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public List<AlgorithmRecordResponse> findAll() {

        return algorithmRecordMapper.selectAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlgorithmRecordResponse> findByUserId(Long userId) {

        return algorithmRecordMapper.selectByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AlgorithmRecordResponse update(Long id, AlgorithmRecordUpdateRequest req) throws IOException {
        AlgorithmRecord record = algorithmRecordMapper.selectById(id);
        if (record == null) {

            throw new AlgorithmRecordNotFoundException(id);
        }

        if (req.getProblemNumber() != null) record.setProblemNumber(req.getProblemNumber());
        if (req.getTitle() != null) record.setTitle(req.getTitle());
        if (req.getLanguage() != null) record.setLanguage(req.getLanguage());
        
        if (req.getFile() != null && !req.getFile().isEmpty()) {
            String code = new String(req.getFile().getBytes(), StandardCharsets.UTF_8);
            record.setCode(code);
        }
        
        record.setUpdatedAt(LocalDateTime.now());

        algorithmRecordMapper.update(record);

        return toResponse(record);
    }

    @Transactional
    public void delete(Long id) {
        int deleted = algorithmRecordMapper.delete(id);
        if (deleted == 0) {

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
