package com.ssafy.dash.shop.presentation;

import com.ssafy.dash.decoration.domain.Decoration;
import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;
import com.ssafy.dash.shop.application.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Shop", description = "상점 및 아이템 API")
@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @Operation(summary = "상점 아이템 목록 조회", description = "구매 가능한 닉네임 꾸미기 아이템 목록을 조회합니다.")
    @GetMapping("/items")
    public ResponseEntity<List<Decoration>> getShopItems() {
        return ResponseEntity.ok(shopService.getShopItems());
    }

    @Operation(summary = "아이템 구매", description = "Log를 사용하여 아이템을 구매합니다.")
    @PostMapping("/buy/{itemId}")
    public ResponseEntity<Void> buyItem(
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal,
            @PathVariable Long itemId) {

        if (principal instanceof CustomOAuth2User customUser) {
            shopService.buyItem(customUser.getUserId(), itemId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "아이템 선물 (관리자)", description = "관리자가 특정 유저에게 아이템을 선물합니다.")
    @PostMapping("/gift")
    public ResponseEntity<Void> giftItem(
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal,
            @RequestBody Map<String, Long> payload) {

        if (principal instanceof CustomOAuth2User customUser) {
            Long targetUserId = payload.get("targetUserId");
            Long itemId = payload.get("itemId");

            if (targetUserId == null || itemId == null) {
                return ResponseEntity.badRequest().build();
            }

            shopService.giftItem(customUser.getUserId(), targetUserId, itemId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
