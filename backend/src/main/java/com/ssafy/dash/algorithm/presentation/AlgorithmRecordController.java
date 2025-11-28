package com.ssafy.dash.algorithm.presentation;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.dash.algorithm.application.AlgorithmRecordService;
import com.ssafy.dash.algorithm.application.dto.AlgorithmRecordCreateRequest;
import com.ssafy.dash.algorithm.application.dto.AlgorithmRecordResponse;
import com.ssafy.dash.algorithm.application.dto.AlgorithmRecordUpdateRequest;
import com.ssafy.dash.oauth.application.security.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Parameter;

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
        if (principal instanceof CustomOAuth2User) {
            userId = ((CustomOAuth2User) principal).getUserId();
        }
        
        AlgorithmRecordResponse response = algorithmRecordService.create(userId, req);

        return ResponseEntity.created(URI.create("/api/algorithm-records/" + response.getId())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AlgorithmRecordResponse>> findAll() {
        
        return ResponseEntity.ok(algorithmRecordService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlgorithmRecordResponse> findById(@PathVariable Long id) {
        
        return ResponseEntity.ok(algorithmRecordService.findById(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AlgorithmRecordResponse> update(
            @PathVariable Long id, 
            @ModelAttribute AlgorithmRecordUpdateRequest req) throws IOException {
            
        return ResponseEntity.ok(algorithmRecordService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        algorithmRecordService.delete(id);

        return ResponseEntity.noContent().build();
    }

}
