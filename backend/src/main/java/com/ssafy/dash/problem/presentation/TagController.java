package com.ssafy.dash.problem.presentation;

import com.ssafy.dash.problem.application.SkillTreeService;
import com.ssafy.dash.problem.application.TagPrerequisiteService;
import com.ssafy.dash.problem.presentation.dto.SkillGraphResponse;
import com.ssafy.dash.problem.presentation.dto.SkillTreeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 태그 및 스킬 트리 API 컨트롤러
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final SkillTreeService skillTreeService;
    private final TagPrerequisiteService prerequisiteService;

    /**
     * 사용자 스킬 트리 조회 (카드 뷰용)
     */
    @GetMapping("/user/{userId}/skill-tree")
    public ResponseEntity<SkillTreeResponse> getUserSkillTree(@PathVariable Long userId) {
        SkillTreeResponse response = skillTreeService.getSkillTree(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 그래프 스킬 트리 조회 (노드/엣지 형태)
     */
    @GetMapping("/graph/{userId}")
    public ResponseEntity<SkillGraphResponse> getSkillGraph(@PathVariable Long userId) {
        SkillGraphResponse response = prerequisiteService.getSkillGraph(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 기본 선수 관계 데이터 초기화 (개발용)
     */
    @PostMapping("/prerequisites/init")
    public ResponseEntity<String> initPrerequisites() {
        prerequisiteService.initializeDefaultPrerequisites();
        return ResponseEntity.ok("Prerequisites initialized");
    }
}
