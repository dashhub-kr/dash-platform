package com.ssafy.dash.user.presentation;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;
import com.ssafy.dash.user.application.UserService;
import com.ssafy.dash.user.presentation.dto.request.UserCreateRequest;
import com.ssafy.dash.user.presentation.dto.request.UserUpdateRequest;
import com.ssafy.dash.user.presentation.dto.response.UserResponse;

import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody UserCreateRequest req) {
        UserResponse created = UserResponse.from(service.create(req.toCommand()));
        
        return ResponseEntity.created(URI.create("/api/users/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable Long id) {

        return ResponseEntity.ok(UserResponse.from(service.findById(id)));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> list() {

        List<UserResponse> responses = service.findAll().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal) {
        if (principal instanceof CustomOAuth2User customUser) {
            
            return ResponseEntity.ok(UserResponse.from(service.findById(customUser.getUserId())));
        }
        
        return ResponseEntity.status(401).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @RequestBody UserUpdateRequest req) {

        return ResponseEntity.ok(UserResponse.from(service.update(id, req.toCommand())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);

        return ResponseEntity.noContent().build();
    }

}
