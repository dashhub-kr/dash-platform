# 4. ER Diagram (ER 다이어그램)

## 4.1 다이어그램
```mermaid
erDiagram
    users ||--o{ user_oauth_tokens : "has"
    users ||--o{ algorithm_records : "creates"
    users ||--o{ boards : "writes"
    users ||--o{ comments : "writes"
    users ||--o{ study_applications : "applies"
    users ||--o{ acorn_log : "logs"

    studies ||--o{ users : "contains"
    studies ||--o{ study_applications : "receives"
    studies ||--o{ acorn_log : "logs"
    studies ||--o{ study_missions : "manages"

    boards ||--o{ comments : "has"
    boards ||--o| algorithm_records : "links"

    study_missions ||--o{ study_mission_submissions : "tracks"
    users ||--o{ study_mission_submissions : "submits"

    tags ||--o{ problem_tags : "categorizes"
    problems ||--o{ problem_tags : "tagged"
    tag_families ||--o{ tags : "groups"

    users {
        BIGINT id PK
        VARCHAR username
        VARCHAR email
        VARCHAR solvedac_handle
        INT solvedac_tier
        INT solved_count
        BIGINT study_id FK
    }

    studies {
        BIGINT id PK
        VARCHAR name
        BIGINT creator_id
        INT acorn_count
        VARCHAR visibility
    }

    algorithm_records {
        BIGINT id PK
        BIGINT user_id FK
        VARCHAR problem_number
        LONGTEXT code
        VARCHAR language
        VARCHAR record_type
    }

    boards {
        BIGINT id PK
        VARCHAR title
        TEXT content
        BIGINT user_id FK
        BIGINT algorithm_record_id FK
    }

    comments {
        BIGINT id PK
        BIGINT board_id FK
        BIGINT user_id FK
        INT line_number
        TEXT content
    }

    study_missions {
        BIGINT id PK
        BIGINT study_id FK
        INT week
        TEXT problem_ids
        DATE deadline
    }

    problems {
        VARCHAR problem_number PK
        VARCHAR title
        INT level
    }
```

## 4.2 주요 테이블 설명
- **users**: 사용자 정보를 저장하며, `study_id`를 통해 소속된 스터디를 참조한다.
- **algorithm_records**: 사용자의 문제 풀이 기록을 저장한다. `record_type`으로 일반 풀이, 모의고사, 랜덤 디펜스 등을 구분한다.
- **study_missions**: 스터디의 주차별 미션 정보를 저장한다. `problem_ids`는 JSON 배열 형태로 문제 목록을 저장한다.
- **boards & comments**: 게시판과 댓글 기능을 담당한다. `comments` 테이블의 `line_number`는 코드 리뷰 시 특정 라인을 지칭하는 데 사용된다.
