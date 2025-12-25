# 2. Use-Case Diagram (Use‑Case 다이어그램)

## 2.1 다이어그램

```mermaid
flowchart LR
    subgraph Actors
        User((사용자))
        Leader((스터디장))
        AI((AI 서비스))
        External((외부 시스템))
    end

    subgraph 회원관리["회원 관리"]
        UC_Login[로그인 GitHub]
        UC_MyPage[마이페이지 조회]
        UC_Sync[프로필 통계 갱신]
    end

    subgraph 스터디["스터디"]
        UC_CreateStudy[스터디 생성]
        UC_JoinStudy[스터디 가입 신청]
        UC_DoMission[주차별 미션 수행]
        UC_ManageMission[미션 관리]
    end

    subgraph 학습훈련["학습 & 훈련"]
        UC_Submit[문제 풀이 제출]
        UC_Defense[랜덤 디펜스 도전]
        UC_MockExam[모의고사 응시]
        UC_SkillTree[스킬트리 확인]
    end

    subgraph AI서비스["AI 서비스"]
        UC_AI_Analysis[AI 코드 분석]
        UC_AI_Tutor[AI 튜터 대화]
        UC_CounterExample[반례 생성]
    end

    subgraph 커뮤니티["커뮤니티"]
        UC_Post[게시글 작성]
        UC_CodeReview[코드 라인 리뷰]
    end

    %% User connections
    User --> UC_Login
    User --> UC_MyPage
    User --> UC_JoinStudy
    User --> UC_DoMission
    User --> UC_Submit
    User --> UC_Defense
    User --> UC_MockExam
    User --> UC_SkillTree
    User --> UC_AI_Analysis
    User --> UC_AI_Tutor
    User --> UC_Post

    %% Leader inherits from User
    Leader -.-> User
    Leader --> UC_CreateStudy
    Leader --> UC_ManageMission

    %% External system connections
    UC_Login --> External
    UC_Sync --> External
    UC_Submit --> External

    %% AI connections
    AI --> UC_AI_Analysis
    AI --> UC_AI_Tutor
    AI --> UC_CounterExample
```

## 2.2 배우(Actor) 정의
| 액터 | 설명 |
|:---:|:---|
| **사용자 (User)** | 알고리즘 문제를 풀고 학습하는 일반 회원 |
| **스터디장 (Leader)** | 스터디를 생성하고 미션을 관리하는 권한을 가진 사용자 (사용자 역할 상속) |
| **AI 서비스** | 코드 분석, 반례 생성, 튜터링을 제공하는 시스템 액터 |
| **외부 시스템** | GitHub (OAuth, Webhook) 및 Solved.ac API |

## 2.3 주요 Use Case 설명
| Use Case | 설명 |
|:---|:---|
| **로그인** | GitHub OAuth를 통한 소셜 로그인 |
| **주차별 미션 수행** | 스터디에서 할당된 주간 문제 풀이 |
| **랜덤 디펜스** | 티어 기반 랜덤 문제 제한시간 내 풀이 |
| **AI 코드 분석** | 제출 코드의 복잡도, 패턴, 개선점 분석 |
| **코드 라인 리뷰** | 특정 코드 라인에 대한 피드백 작성 |
