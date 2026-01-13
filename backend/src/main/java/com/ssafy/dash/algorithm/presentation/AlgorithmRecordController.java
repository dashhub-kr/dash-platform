package com.ssafy.dash.algorithm.presentation;

import com.ssafy.dash.algorithm.application.AlgorithmRecordService;
import com.ssafy.dash.algorithm.presentation.dto.request.AlgorithmRecordUpdateRequest;
import com.ssafy.dash.algorithm.presentation.dto.response.AlgorithmRecordResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/algorithm")
@RequiredArgsConstructor
@Tag(name = "Algorithm Record", description = "알고리즘 풀이 기록 관리 API")
@SuppressWarnings("null")
public class AlgorithmRecordController {

    private final AlgorithmRecordService algorithmRecordService;

    @GetMapping
    public ResponseEntity<List<AlgorithmRecordResponse>> findAll() {
        List<AlgorithmRecordResponse> responses = algorithmRecordService.findAll().stream()
                .map(AlgorithmRecordResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/study/{studyId}")
    public ResponseEntity<List<AlgorithmRecordResponse>> findByStudyId(@PathVariable Long studyId) {
        List<AlgorithmRecordResponse> responses = algorithmRecordService.findByStudyId(studyId).stream()
                .map(AlgorithmRecordResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AlgorithmRecordResponse>> findByUserId(@PathVariable Long userId) {
        List<AlgorithmRecordResponse> responses = algorithmRecordService.findByUserId(userId).stream()
                .map(AlgorithmRecordResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlgorithmRecordResponse> findById(@PathVariable Long id) {
        AlgorithmRecordResponse response = AlgorithmRecordResponse.from(algorithmRecordService.findById(id));

        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AlgorithmRecordResponse> update(
            @PathVariable Long id,
            @ModelAttribute AlgorithmRecordUpdateRequest req) throws IOException {
        AlgorithmRecordResponse response = AlgorithmRecordResponse
                .from(algorithmRecordService.update(id, req.toCommand()));

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        algorithmRecordService.delete(id);

        return ResponseEntity.noContent().build();
    }

}
