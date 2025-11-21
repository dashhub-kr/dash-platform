# dash - Backend

간단한 백엔드 README입니다.  
Spring Boot ( Maven ) 기반으로 작성되어 있습니다.

## 요구사항
- Java 17
- MySQL
- Maven

## 빠른 시작
1. `backend` 디렉터리로 이동

```bash
cd backend
```

2. Maven Wrapper로 실행

Linux / Git Bash:
```bash
chmod +x mvnw
./mvnw spring-boot:run
```

Windows (PowerShell/CMD):
```powershell
.\mvnw.cmd spring-boot:run
```

또는 패키징 후 실행:
```bash
./mvnw package
java -jar target/dash-0.0.1-SNAPSHOT.jar
```

## 설정
- `src/main/resources/application.properties`에서 DB 연결 정보를 수정하세요.
- 초기 스키마는 `src/main/resources/schema.sql`에 있습니다. MySQL을 사용하는 경우 해당 SQL을 DB에 적용하세요.

예) application.properties 예시
```
spring.datasource.url=jdbc:mysql://localhost:3306/dash_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=ssafy
spring.datasource.password=ssafy
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=none
```

## 주요 엔드포인트
- 사용자 목록 조회: `GET /api/users`
- 사용자 생성 (회원가입): `POST /api/users`
  - Body(JSON) 예:
    ```json
    {
      "githubId": 12345678,
      "username": "tester",
      "avatarUrl": "https://example.com/avatar.png",
      "solvedAcHandle": "solved",
      "accessToken": "token"
    }
    ```
- ID로 조회: `GET /api/users/{id}`
- 깃허브 ID로 조회: `GET /api/users/github/{githubId}`
- 수정: `PUT /api/users/{id}`
- 삭제: `DELETE /api/users/{id}`

예시 curl (회원 생성):
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"githubId":12345678,"username":"tester","avatarUrl":"","solvedAcHandle":"","accessToken":""}'
```

## 주의 사항
- Lombok을 제거했습니다. 현재 소스는 직접 작성한 getter/setter를 사용합니다.
- `.gitattributes`와 `.gitignore`는 루트에 통합되어 있으므로 각 IDE/환경에 맞춰 사용하세요.
- 이미 Git에 추적된 빌드 아티팩트가 있다면 아래 명령으로 캐시에서 제거한 뒤 커밋하세요:
```bash
git rm -r --cached backend/target
git commit -m "Remove tracked backend build artifacts"
```
