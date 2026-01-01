# Study Domain 아키텍처 점검 보고서

**점검 일시**: 2026-01-01  
**표준 도메인**: `user`  
**점검 대상**: `study`

---

## 📋 분석 개요

Study 도메인은 전반적으로 클린 아키텍처를 잘 따르고 있으나, **Presentation Layer와 Application Layer에서 내부 클래스를 DTO로 사용**하는 문제가 있습니다.

---

## ✅ Study Domain 현재 구조

```
study/
├── application/
│   ├── StudyService.java
│   ├── StudyAnalysisService.java
│   ├── StudyMissionService.java
│   └── dto/
│       └── result/
│           └── StudyStatsResult.java
├── domain/
│   ├── Study.java (Entity)
│   ├── StudyApplication.java (Entity)
│   ├── StudyMission.java (Entity)
│   ├── StudyMissionSubmission.java (Entity)
│   ├── StudyRepository.java (Interface)
│   ├── StudyMissionRepository.java (Interface)
│   ├── StudyMissionSubmissionRepository.java (Interface)
│   └── StudyVisibility.java (Enum)
├── infrastructure/
│   ├── mapper/
│   │   ├── StudyMapper.java
│   │   ├── StudyMissionMapper.java
│   │   └── StudyMissionSubmissionMapper.java
│   └── persistence/
│       ├── StudyRepositoryImpl.java
│       ├── StudyMissionRepositoryImpl.java
│       └── StudyMissionSubmissionRepositoryImpl.java
└── presentation/
    ├── StudyController.java
    └── dto/
        ├── CreateStudyRequest.java
        └── response/
            ├── StudyListResponse.java
            └── StudyStatsResponse.java
```

---

## 🚨 아키텍처 위반 사항

### 1. **Controller 내부 record 사용** ⚠️ **MEDIUM**

> [!IMPORTANT]
> StudyController에 5개의 내부 record가 정의되어 있어 재사용성과 테스트 가능성이 떨어집니다.

**위반 코드** ([StudyController.java](file:///c:/dash/backend/src/main/java/com/ssafy/dash/study/presentation/StudyController.java)):

```java
// Line 146
public record ApplyStudyRequest(String message) {}

// Line 225-230
public record CreateMissionRequest(
    Integer week,
    String title,
    List<Integer> problemIds,
    java.time.LocalDate deadline) {}

// Line 232-234
public record AddMissionProblemsRequest(List<Integer> problemIds) {}

// Line 279-282
public record UpdateMissionRequest(
    String title,
    java.time.LocalDate deadline) {}

// Line 296-297
public record UpdateMissionStatusRequest(String status) {}
```

**문제점**:
- DTO를 다른 곳에서 재사용 불가
- 단위 테스트 작성 시 Controller를 import해야 함
- 코드 가독성 저하

---

### 2. **Service 내부 클래스를 응답 DTO로 사용** ⚠️ **HIGH**

> [!WARNING]
> Application Layer의 Service 클래스 내부에 정의된 클래스를 Presentation Layer에서 직접 반환하고 있습니다.

**위반 코드**:

#### StudyAnalysisService 내부 클래스 ([StudyAnalysisService.java](file:///c:/dash/backend/src/main/java/com/ssafy/dash/study/application/StudyAnalysisService.java))
```java
// Controller에서 직접 사용
public ResponseEntity<StudyAnalysisService.StudyAnalysisResult> getStudyAnalysis(...) // L165
public ResponseEntity<List<StudyAnalysisService.TeamFamilyStat>> getTeamFamilyStats(...) // L172
```

#### StudyMissionService 내부 클래스 ([StudyMissionService.java](file:///c:/dash/backend/src/main/java/com/ssafy/dash/study/application/StudyMissionService.java))
```java
// Controller에서 직접 사용
public ResponseEntity<List<StudyMissionService.MissionWithProgress>> getMissions(...) // L188
public ResponseEntity<List<StudyMissionService.MemberProgress>> getMissionProgress(...) // L209
```

**문제점**:
- Application Layer와 Presentation Layer 간 강결합
- Service 내부 구현이 API 응답 스펙에 직접 노출됨
- 계층 간 책임 분리 원칙 위반

---

### 3. **StudyRepository가 StudyApplication도 관리** ⚠️ **LOW**

> [!NOTE]
> StudyRepository가 Study와 StudyApplication 두 엔티티를 모두 관리하고 있습니다.

**위반 코드** ([StudyRepository.java](file:///c:/dash/backend/src/main/java/com/ssafy/dash/study/domain/StudyRepository.java#L18-L30)):
```java
public interface StudyRepository {
    // Study 관련
    void save(Study study);
    Optional<Study> findById(Long id);
    
    // StudyApplication 관련 (SRP 위반)
    void saveApplication(StudyApplication application);
    Optional<StudyApplication> findApplicationById(Long id);
    void updateApplicationStatus(StudyApplication application);
    // ...
}
```

**문제점**:
- Single Responsibility Principle (SRP) 위반
- Repository가 두 개의 엔티티를 관리

**권장사항**: `StudyApplicationRepository` 별도 분리 (선택사항)

---

## 📊 비교 요약표

| 항목 | User Domain (표준) | Study Domain (현재) | 준수 여부 |
|------|-------------------|---------------------|----------|
| **Domain Layer** | ✅ Entity + Repository Interface | ✅ Entity + Repository Interface | ✅ |
| **Infrastructure Layer** | ✅ RepositoryImpl + Mapper | ✅ RepositoryImpl + Mapper | ✅ |
| **Application Layer** | ✅ Service + Result DTO (별도 파일) | ⚠️ Service + 내부 클래스 | ⚠️ |
| **Presentation Layer** | ✅ Controller + Request/Response DTO (별도 파일) | ⚠️ Controller + 내부 record | ⚠️ |
| **계층 간 격리** | ✅ DTO를 통한 명확한 분리 | ❌ Service 내부 클래스 직접 노출 | ❌ |

---

## 🔧 권장 개선 사항

### 1. Presentation Layer DTO 분리

```
presentation/dto/
├── request/
│   ├── ApplyStudyRequest.java
│   ├── CreateMissionRequest.java
│   ├── AddMissionProblemsRequest.java
│   ├── UpdateMissionRequest.java
│   └── UpdateMissionStatusRequest.java
└── response/
    ├── StudyAnalysisResponse.java
    ├── TeamFamilyStatResponse.java
    ├── MissionWithProgressResponse.java
    └── MemberProgressResponse.java
```

### 2. Application Layer Result DTO 분리

```
application/dto/result/
├── StudyAnalysisResult.java
├── TeamFamilyStatResult.java
├── MissionWithProgressResult.java
└── MemberProgressResult.java
```

### 3. Controller 수정

Service 내부 클래스 대신 Response DTO 사용:

```java
// 변경 전
public ResponseEntity<StudyAnalysisService.StudyAnalysisResult> getStudyAnalysis(...)

// 변경 후
public ResponseEntity<StudyAnalysisResponse> getStudyAnalysis(...)
```

---

## 📌 결론

> [!IMPORTANT]
> **Study 도메인은 대부분 클린 아키텍처를 잘 따르고 있으나, DTO 분리가 필요합니다.**

**주요 문제점**:
1. ⚠️ Controller 내부 record 5개 (별도 파일로 분리 필요)
2. ⚠️ Service 내부 클래스를 Presentation에서 직접 사용 (계층 간 강결합)
3. ⚠️ StudyRepository가 StudyApplication도 관리 (SRP 위반, 선택적 개선)

**개선 우선순위**:
1. **HIGH**: Service 내부 클래스를 Application Result DTO와 Presentation Response DTO로 분리
2. **MEDIUM**: Controller 내부 record를 별도 Request DTO 파일로 분리
3. **LOW**: StudyApplicationRepository 분리 (선택사항)

User 도메인 표준에 맞추려면 **DTO 분리 리팩토링**이 필요합니다.
