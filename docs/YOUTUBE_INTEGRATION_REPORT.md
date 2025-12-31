# 유튜브 영상 검색 기능 구현 작업 내역

## 1. Backend (Spring Boot)

유튜브 Data API를 활용하여 검색 기능을 제공하기 위해 **Domain-Infrastructure-Application-Presentation** 계층형 아키텍처를 따랐습니다.

### 1-1. Domain Layer (도메인 계층)
비즈니스 핵심 개념과 인터페이스를 정의했습니다.
- **`com.ssafy.dash.youtube.domain.YouTubeClient` (Interface)**: 외부 API와의 추상화된 통신 인터페이스입니다. `searchVideos(String keyword)` 메서드를 정의하여 구현체가 변경되더라도 도메인 로직은 영향받지 않도록 했습니다.
- **`com.ssafy.dash.youtube.domain.YouTubeVideo` (Record)**: 유튜브 영상 데이터(ID, 제목, 설명, 썸네일, 게시일 등)를 담는 불변 객체(DTO)입니다.
- **`com.ssafy.dash.youtube.domain.exception.YouTubeException`**: 유튜브 API 호출 중 발생하는 예외를 처리하기 위한 커스텀 예외 클래스입니다.

### 1-2. Infrastructure Layer (인프라 계층)
실제 외부 API 툴을 사용하여 도메인 인터페이스를 구현했습니다.
- **`com.ssafy.dash.youtube.infrastructure.client.YouTubeClientImpl`**: `YouTubeClient`의 구현체입니다. Spring의 `RestTemplate`을 사용하여 `https://www.googleapis.com/youtube/v3/search` 엔드포인트에 GET 요청을 보냅니다. JSON 응답을 `ObjectMapper`로 파싱하여 `YouTubeVideo` 객체 리스트로 변환합니다.
- **`com.ssafy.dash.youtube.config.YouTubeProperties`**: `application.properties`에서 `youtube.api-key` 값을 안전하게 바인딩하여 관리하는 설정 클래스입니다.
- **`com.ssafy.dash.youtube.config.YouTubeConfig`**: 위 Properties 클래스를 활성화하는 설정 파일입니다.

### 1-3. Application Layer (응용 계층)
- **`com.ssafy.dash.youtube.application.YouTubeService`**: 컨트롤러와 클라이언트 사이를 연결하는 서비스 레이어입니다. 현재는 단순 통과(pass-through) 로직이지만, 향후 캐싱이나 추가 비즈니스 로직(예: 검색 기록 저장)을 붙일 수 있는 확장 포인트입니다.

### 1-4. Presentation Layer (표현 계층)
- **`com.ssafy.dash.youtube.presentation.YouTubeController`**: REST API 엔드포인트 `GET /api/youtube/search`를 제공합니다. 프론트엔드로부터 `keyword` 파라미터를 받아 서비스를 호출하고, 결과를 JSON 형태로 반환합니다.
- **`com.ssafy.dash.youtube.presentation.dto.response.YouTubeResponse`**: 클라이언트에게 필요한 데이터만 선별하여 내려주기 위한 응답 DTO입니다.

---

## 2. Frontend (Vue.js)

사용자가 키워드를 입력하고 결과를 시각적으로 확인할 수 있는 UI를 구현했습니다.

### 2-1. API Module
- **`src/api/youtube.js`**: `axios` 인스턴스(`http.js`)를 사용하여 백엔드의 `/api/youtube/search` 엔드포인트를 호출하는 `search(keyword)` 함수를 정의했습니다.

### 2-2. View (화면)
- **`src/views/YouTubeSearch.vue`**:
    - **검색창**: 사용자가 검색어를 입력하고 엔터 또는 버튼 클릭 시 검색을 수행합니다.
    - **결과 그리드**: 검색 결과를 카드 형태(썸네일, 제목, 채널명, 게시일)로 보여줍니다.
    - **인터랙션**: 카드 클릭 시 해당 유튜브 영상 페이지를 새 탭으로 엽니다. 로딩 중일 때는 스피너를 보여주며, 결과가 없을 때의 UI도 처리했습니다.
    - **스타일**: 기존의 다크 테마(`bg-slate-950`)와 `Tailwind CSS`를 사용하여 일관된 디자인을 유지했습니다.

### 2-3. Router & Navigation
- **`src/router/index.js`**: `/youtube` 경로에 `YouTubeSearch` 컴포넌트를 매핑했습니다.
- **`src/views/Dashboard.vue`**: 대시보드의 '빠른 이동' 섹션에 '영상 학습' 카드를 추가하여 사용자가 쉽게 접근할 수 있도록 했습니다. `lucide-vue-next`의 `Youtube` 아이콘을 사용하여 시인성을 높였습니다.
