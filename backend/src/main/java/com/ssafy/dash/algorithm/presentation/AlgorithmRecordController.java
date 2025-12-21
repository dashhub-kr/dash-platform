package com.ssafy.dash.algorithm.presentation;

import com.ssafy.dash.algorithm.application.AlgorithmRecordService;
import com.ssafy.dash.algorithm.presentation.dto.request.AlgorithmRecordCreateRequest;
import com.ssafy.dash.algorithm.presentation.dto.request.AlgorithmRecordUpdateRequest;
import com.ssafy.dash.algorithm.presentation.dto.response.AlgorithmRecordResponse;
import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/algorithm-records")
public class AlgorithmRecordController {

    private final AlgorithmRecordService algorithmRecordService;

    public AlgorithmRecordController(AlgorithmRecordService algorithmRecordService) {
        this.algorithmRecordService = algorithmRecordService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AlgorithmRecordResponse> create(
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal,
            @ModelAttribute AlgorithmRecordCreateRequest req) throws IOException {
        Long userId = 1L;
        if (principal instanceof CustomOAuth2User customUser) {
            userId = customUser.getUserId();
        }

        AlgorithmRecordResponse response = AlgorithmRecordResponse
                .from(algorithmRecordService.create(req.toCommand(userId)));

        return ResponseEntity.created(URI.create("/api/algorithm-records/" + response.id())).body(response);
    }

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
