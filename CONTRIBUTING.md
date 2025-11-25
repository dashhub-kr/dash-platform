# 기여 가이드 (CONTRIBUTING)

이 프로젝트에 기여해주셔서 감사합니다!  
원활한 협업을 위해 아래의 가이드를 따라주세요.

## 1. 프로젝트 구조

이 저장소는 백엔드(`backend`)와 프론트엔드(`frontend`)로 구성되어 있습니다.  
본 가이드는 주로 **백엔드(Spring Boot)** 개발에 초점을 맞추고 있습니다.

## 2. 브랜치 전략

Git Flow를 기반으로 한 간단한 전략을 사용합니다.

- `main`: 배포 가능한 안정적인 상태
- `develop`: 다음 배포를 위한 개발 브랜치 (필요 시)
- `feature/{기능명}`: 새로운 기능 개발 (예: `feature/algorithm-record`)
- `fix/{버그명}`: 버그 수정
- `refactor/{대상}`: 리팩토링

## 3. 커밋 컨벤션

커밋 메시지는 명확하고 일관성 있게 작성합니다.

- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 수정
- `style`: 코드 포맷팅, 세미콜론 누락 등 (코드 변경 없음)
- `refactor`: 코드 리팩토링
- `test`: 테스트 코드 추가/수정
- `chore`: 빌드 업무 수정, 패키지 매니저 수정

예시: `feat(BE): 알고리즘 기록 CRUD API 구현`

## 4. 코드 스타일

- **Java Version**: 17
- **Framework**: Spring Boot 3.x
- **Naming**:
  - Class: PascalCase
  - Method/Variable: camelCase
  - Constant: UPPER_SNAKE_CASE
  - DB Table/Column: snake_case

## 5. 개발 프로세스 (TDD & DDD)

우리는 **TDD(Test Driven Development)**, **DDD(Domain Driven Design)** 스타일의 계층형 아키텍처를 지향합니다.

1. **도메인 정의**: `domain` 패키지에 엔티티 정의
2. **테스트 작성**: 실패하는 테스트 먼저 작성 (Service → Controller 순)
3. **기능 구현**: 테스트를 통과하기 위한 최소한의 코드 구현
4. **리팩토링**: 중복 제거, 가독성 향상 (`TestFixtures` 활용 등)

자세한 내용은 `docs/` 디렉토리의 문서를 참고해주세요.

- [테스트 가이드](backend/docs/TESTING_GUIDE.md)
- [아키텍처 가이드](backend/docs/ARCHITECTURE.md)
