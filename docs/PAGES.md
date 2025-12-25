# 프론트엔드 페이지 구조 및 기능 설명

## 개요
이 문서는 Dash 프로젝트의 프론트엔드 페이지 구성과 각 페이지의 주요 기능을 설명합니다.

---

## 페이지 구조

### 1. 랜딩 페이지 (Landing)
- **경로**: `/`
- **파일**: `src/views/LandingView.vue`
- **기능**: 서비스 소개, 주요 기능 하이라이트, GitHub 로그인 진입점 제공.

### 2. 온보딩 (Onboarding)
- **경로**: `/onboarding`
- **파일**: `src/views/onboarding/OnboardingView.vue`
- **기능**: 신규 사용자의 Solved.ac 핸들 등록, GitHub 리포지토리 설정, 스터디 가입 안내.

### 3. 대시보드 (Dashboard)
- **경로**: `/dashboard`
- **파일**: `src/views/Dashboard.vue`
- **기능**:
    - 사용자 환영 메시지 및 프로필 요약.
    - 통계 카드 (문제 풀이 수, Streak, 티어).
    - 오늘의 추천 문제, 최근 활동 피드.
    - AI 힌트 및 코드 분석 빠른 접근.

### 4. 트레이닝 (Training)
사용자 맞춤형 학습 경로를 제공하는 영역입니다.

#### 4.1 학습 로드맵 (Roadmap)
- **경로**: `/training/roadmap`
- **파일**: `src/views/training/TrainingRoadmapView.vue`
- **기능**: AI가 추천하는 단계별 학습 로드맵, 각 단계의 목표 및 추천 문제 제공.

#### 4.2 스킬 트리 (Skill Tree)
- **경로**: `/training/skilltree`
- **파일**: `src/views/training/TrainingSkillTreeView.vue`
- **기능**: 알고리즘 태그별 숙련도를 그래프로 시각화, 부족한 영역 파악 및 학습 자료 연결.

### 5. 랜덤 디펜스 (Defense)
- **경로**: `/defense`
- **파일**: `src/views/DefenseView.vue`
- **기능**:
    - 실버/골드 난이도 선택 후 랜덤 문제 도전.
    - 제한 시간 내 풀이, 연속 성공 Streak 기록.

### 6. 모의고사 (Mock Exam)
- **경로**: `/mockexam`
- **파일**: `src/views/MockExamView.vue`
- **기능**:
    - IM, A, B, 삼성, 카카오 등 실제 코딩 테스트 유형 시뮬레이션.
    - 제한 시간 및 문제 세트 제공, 결과 리포트.

### 7. 스터디 (Study)
스터디 그룹 관련 기능을 제공합니다.

#### 7.1 스터디 분석 (Analysis)
- **경로**: `/study/analysis`
- **파일**: `src/views/StudyAnalysisView.vue`
- **기능**: 팀원별 실력 분석, 레이더 차트, AI 기반 커리큘럼 추천.

#### 7.2 스터디 미션 (Missions)
- **경로**: `/study/missions`
- **파일**: `src/views/StudyMissionView.vue`
- **기능**: 주차별 미션 목록, 문제별 제출 현황 대시보드, SOS 요청.

#### 7.3 스터디 랭킹/목록 (List)
- **경로**: `/study/ranking`
- **파일**: `src/views/StudyListView.vue`
- **기능**: 전체 스터디 목록 조회, 필터링, 가입 신청.

### 8. 게시판 (Board)
커뮤니티 기능을 담당합니다.

#### 8.1 게시글 목록 (List)
- **경로**: `/boards`
- **파일**: `src/views/BoardList.vue`
- **기능**: 게시글 목록, 검색, 글쓰기 진입.

#### 8.2 게시글 상세 (Detail)
- **경로**: `/boards/:id`
- **파일**: `src/views/BoardDetail.vue`
- **기능**: 게시글 내용, 댓글(라인 리뷰 포함), 좋아요.

#### 8.3 게시글 작성/수정 (Form)
- **경로**: `/boards/write`, `/boards/edit/:id`
- **파일**: `src/views/BoardForm.vue`
- **기능**: Markdown 에디터, 알고리즘 풀이 코드 첨부.

### 9. 프로필 (Profile)
- **경로**: `/profile`
- **파일**: `src/views/Profile.vue`
- **기능**: 사용자 정보 조회/수정, Solved.ac 연동 관리, 회원 탈퇴.

### 10. YouTube 검색
- **경로**: `/youtube`
- **파일**: `src/views/YouTubeSearch.vue`
- **기능**: 알고리즘 관련 강의 영상 검색 및 시청.

---

## 디자인 스타일
- **Tailwind CSS** 기반의 라이트 모드 (Dash Blue `#3396F4` 브랜드 컬러).
- **일관된 레이아웃**: 2-Column (Main + Right Sidebar) 표준 구조.
- **애니메이션**: 페이지 진입 시 부드러운 페이드 인/업 애니메이션.
- **디자인 시스템**: `frontend/DESIGN_SYSTEM.md` 참조.
