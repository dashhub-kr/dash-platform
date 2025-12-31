# 3. Class Diagram (클래스 다이어그램)

## 3.1 다이어그램

```mermaid
classDiagram
    direction TB

    class User {
        +Long id
        +String username
        +String email
        +String solvedacHandle
        +Integer solvedacTier
        +Integer acornCount
        +String provider
        +create(username, email, ...)
        +updateProfile(...)
        +updateSolvedacProfile(...)
    }

    class Study {
        +Long id
        +String name
        +Long creatorId
        +Integer acornCount
        +StudyVisibility visibility
        +String activeMissionTitle
        +addAcorns(amount)
        +useAcorns(amount)
    }

    class StudyApplication {
        +Long id
        +Long studyId
        +Long userId
        +String status
        +String message
    }

    class AlgorithmRecord {
        +Long id
        +Long userId
        +String problemNumber
        +String title
        +String code
        +String language
        +String recordType
        +String tag
    }

    class Board {
        +Long id
        +String title
        +String content
        +Long userId
        +String boardType
        +Integer likeCount
    }

    class Comment {
        +Long id
        +Long boardId
        +Long userId
        +String content
        +Integer lineNumber
        +Long parentId
    }

    class Problem {
        +String problemNumber
        +String title
        +Integer level
        +Boolean essential
    }

    User "1" --> "0..*" StudyApplication : applies
    Study "1" --> "0..*" StudyApplication : receives
    User "1" --> "0..1" Study : joins
    User "1" --> "0..*" AlgorithmRecord : solves
    User "1" --> "0..*" Board : writes
    User "1" --> "0..*" Comment : writes
    Board "1" --> "0..*" Comment : has
    AlgorithmRecord "0..1" -- "0..1" Board : references
    Problem "1" -- "0..*" AlgorithmRecord : recorded in
```

## 3.2 주요 클래스 설명
- **User**: 시스템의 핵심 엔티티로, 사용자 기본 정보와 Solved.ac 연동 정보, 학습 통계 등을 관리한다.
- **Study**: 스터디 그룹을 나타내며, 멤버십, 도토리(재화), 진행 중인 미션 정보를 포함한다.
- **AlgorithmRecord**: 사용자가 푼 알고리즘 문제에 대한 기록으로, 제출 코드, 언어, 풀이 시간 등을 저장한다.
- **Board**: 커뮤니티 게시글 엔티티로, 일반 게시글과 질문 게시글 등을 구분한다.
- **Comment**: 게시글에 달리는 댓글이며, `lineNumber` 필드를 통해 특정 코드 라인에 대한 피드백(코드 리뷰) 기능을 지원한다.