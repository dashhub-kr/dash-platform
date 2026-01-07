package com.ssafy.dash.decoration.presentation;

import com.ssafy.dash.decoration.application.DecorationService;
import com.ssafy.dash.decoration.domain.Decoration;
import com.ssafy.dash.decoration.domain.UserDecoration;
import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DecorationController {

    private final DecorationService decorationService;

    public DecorationController(DecorationService decorationService) {
        this.decorationService = decorationService;
    }

    // --- 관리자 엔드포인트 (Admin Endpoints) ---

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/decorations")
    public ResponseEntity<Decoration> createDecoration(@RequestBody DecorationCreateRequest request) {
        Decoration d = decorationService.create(request.name(), request.description(), request.cssClass(), request.type());
        return ResponseEntity.ok(d);
    }

    @GetMapping("/admin/decorations")
    public ResponseEntity<List<Decoration>> getAllDecorations() {
        return ResponseEntity.ok(decorationService.findAll());
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/decorations/{id}")
    public ResponseEntity<Void> deleteDecoration(@PathVariable Long id) {
        decorationService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/decorations/grant")
    public ResponseEntity<Void> grantDecoration(@RequestBody DecorationGrantRequest request) {
        decorationService.grant(request.userId(), request.decorationId());
        return ResponseEntity.ok().build();
    }

    // --- 사용자 엔드포인트 (User Endpoints) ---

    @GetMapping("/decorations/mine")
    public ResponseEntity<List<UserDecoration>> getMyInventory(@AuthenticationPrincipal CustomOAuth2User user) {
        return ResponseEntity.ok(decorationService.getInventory(user.getUserId()));
    }

    @PostMapping("/decorations/equip/{id}")
    public ResponseEntity<Void> equip(@AuthenticationPrincipal CustomOAuth2User user, @PathVariable Long id) {
        decorationService.equip(user.getUserId(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/decorations/unequip")
    public ResponseEntity<Void> unequip(@AuthenticationPrincipal CustomOAuth2User user) {
        decorationService.unequip(user.getUserId());
        return ResponseEntity.ok().build();
    }
    
    // DTOs
    public record DecorationCreateRequest(String name, String description, String cssClass, String type) {}
    public record DecorationGrantRequest(Long userId, Long decorationId) {}
}
