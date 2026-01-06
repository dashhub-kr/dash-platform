# 2026-01-06 스터디장 위임 기능 실패

## 이슈 요약 (ISSUE SUMMARY)
2026년 1월 6일, 스터디장 위임(Delegation) 기능이 작동하지 않는다는 이슈가 보고되었습니다. UI를 통해 스터디장 권한 위임을 시도했을 때 성공한 것처럼 보였으나(200 OK 반환), 실제 데이터베이스에는 변경 사항이 반영되지 않았습니다.

## 원인 분석 (ROOT CAUSE ANALYSIS)
백엔드 `StudyMapper.xml` 파일에서 원인을 파악했습니다.
- **컴포넌트**: `StudyMapper`
- **메서드**: `update`
- **결함**: `studies` 테이블에 대한 SQL `UPDATE` 문 내 `SET` 절에서 `creator_id` 필드가 누락되어 있었습니다.
- **영향**: `StudyService` 계층에서는 `creatorId`가 정상적으로 변경되었으나, 영속성 계층(Persistence Layer)에서 이 변경 사항을 DB에 저장하지 못했습니다.

## 해결 방안 (RESOLUTION)
`StudyMapper.xml`의 `update` SQL 문에 `creator_id` 필드를 추가하여 수정했습니다.

### 코드 변경 사항
```xml
<!-- 변경 전 -->
UPDATE studies SET name = #{name}, acorn_count = #{acornCount}, 
visibility = #{visibility}, description = #{description}, streak = #{streak}, 
active_mission_title = #{activeMissionTitle}, active_mission_progress = #{activeMissionProgress},
study_type = #{studyType}
WHERE id = #{id}

<!-- 변경 후 -->
UPDATE studies SET name = #{name}, acorn_count = #{acornCount}, 
visibility = #{visibility}, description = #{description}, streak = #{streak}, 
active_mission_title = #{activeMissionTitle}, active_mission_progress = #{activeMissionProgress},
study_type = #{studyType}, creator_id = #{creatorId}
WHERE id = #{id}
```

## 검증 (VERIFICATION)
- **수동 검증**: 코드 인스펙션을 통해 업데이트 쿼리에 해당 필드가 포함되었음을 확인했습니다.
- **자동화 테스트**: 환경 설정 후 `delegateLeader` 로직에 대한 통합 테스트 통과가 예상됩니다.

## 상태 (STATUS)
**해결됨 (RESOLVED)**
