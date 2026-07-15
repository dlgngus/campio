# Campio 지금 배포하기: Render + PostgreSQL + Vercel

이 문서는 이미 GitHub push, Render PostgreSQL 생성, Render Backend Web Service 생성과 Deploy 클릭까지 끝난 상태에서 이어서 진행하는 순서입니다.

비밀번호, DB URL, API key는 Git에 저장하지 말고 Render/Vercel 환경변수 화면에만 입력하세요.

## 0. 현재 완료된 단계

- GitHub 저장소 push 완료
- Render PostgreSQL 데이터베이스 생성 완료
- Render Backend Web Service 생성 및 첫 Deploy 시도 완료
- Render PostgreSQL Internal Database URL 확인 완료
- 아직 해야 할 일: Render 환경변수 입력, 백엔드 재배포와 로그 확인, Vercel 프론트 배포, 최종 CORS 수정

## 1. Render 백엔드 설정 확인

Render Web Service 설정이 아래와 같아야 합니다.

| 항목 | 값 |
| --- | --- |
| Environment | Docker |
| Root Directory | `campio-backend` |
| Dockerfile Path | 비워두거나 `Dockerfile` |
| Health Check Path | `/api/health` |
| Branch | 배포할 GitHub branch |

`campio-backend/Dockerfile`은 Root Directory가 `campio-backend`일 때 정상 동작하도록 되어 있습니다.

Render는 `PORT` 환경변수를 자동 주입합니다. Campio 백엔드는 `server.port=${PORT:8080}`로 이 값을 사용합니다.

## 2. Render 환경변수 입력 방법

1. Render Dashboard로 이동합니다.
2. Backend Web Service를 클릭합니다.
3. 왼쪽 메뉴에서 `Environment`를 클릭합니다.
4. `Add Environment Variable`을 누릅니다.
5. 아래 표의 값을 입력합니다.
6. 저장 후 `Manual Deploy` -> `Deploy latest commit`을 누릅니다.

## 3. Render Backend 환경변수

권장 방식은 Render PostgreSQL의 Internal Database URL을 `DATABASE_URL`에 그대로 넣는 것입니다.

| 변수명 | 필수 | Render 직접 입력 | 값 형식 예시 | 비밀값 | 현재 코드에서 사용되는 위치 |
| --- | --- | --- | --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | 예 | 예 | `prod` | 아니오 | Spring profile 활성화, `application-prod.yml` |
| `DATABASE_URL` | 예 | 예 | `postgresql://USER:PASSWORD@HOST:5432/DB` | 예 | `DatabaseUrlEnvironmentPostProcessor`가 JDBC URL/username/password로 변환 |
| `SPRING_DATASOURCE_URL` | 대체 방식 | 아니오, `DATABASE_URL` 사용 시 불필요 | `jdbc:postgresql://HOST:5432/DB` 또는 `postgresql://USER:PASSWORD@HOST:5432/DB` | URL에 비밀번호가 있으면 예 | `application-prod.yml`, `DatabaseUrlEnvironmentPostProcessor` |
| `SPRING_DATASOURCE_USERNAME` | 대체 방식 | 아니오, `DATABASE_URL` 사용 시 불필요 | `campio_user` | 예 | `application-prod.yml` |
| `SPRING_DATASOURCE_PASSWORD` | 대체 방식 | 아니오, `DATABASE_URL` 사용 시 불필요 | Render DB password | 예 | `application-prod.yml` |
| `FRONTEND_ORIGIN` | 예 | 예 | `https://your-app.vercel.app` | 아니오 | `CorsConfig`, `campio.frontend-origin` |
| `CAMPIO_SEED_ENABLED` | 아니오 | 아니오 | 사용하지 않음 | 아니오 | 현재 코드에서 사용하지 않음. 샘플 seed 기능 없음 |
| `CAMPIO_ADMIN_EMAIL` | 예 | 예 | `admin@example.com` | 개인정보 | `DataBootstrapper`, 관리자 계정 최초 생성 |
| `CAMPIO_ADMIN_PASSWORD` | 예 | 예 | 강한 비밀번호 | 예 | `DataBootstrapper`, BCrypt 해시 저장 |
| `PORT` | 아니오 | 보통 입력하지 않음 | Render 자동 주입, 로컬은 `8080` | 아니오 | `application.yml`의 `server.port` |
| `CAMPIO_FLYWAY_ENABLED` | 아니오 | 보통 입력하지 않음 | `true` | 아니오 | `FlywayMigrationConfig`; 기본값 `true` |
| `CAMPIO_FLYWAY_BASELINE_ON_MIGRATE` | 아니오 | 보통 입력하지 않음 | `true` | 아니오 | `FlywayMigrationConfig`; 기본값 `true` |
| `CAMPIO_INGESTION_BOOTSTRAP_SOURCES_ENABLED` | 아니오 | 필요할 때만 | `false` 권장 | 아니오 | `DataBootstrapper`; 기본 소스 활성화 여부 |
| `CAMPIO_INGESTION_AUTO_RUN_ON_STARTUP` | 아니오 | 필요할 때만 | `false` 권장 | 아니오 | `IngestionStartupRunner`; 시작 시 자동 수집 여부 |
| `CAMPIO_INGESTION_AUTO_RUN_ONLY_WHEN_EMPTY` | 아니오 | 필요할 때만 | `true` | 아니오 | `IngestionStartupRunner`; 공개 공고가 있으면 자동 수집 스킵 |

### Internal Database URL 넣는 곳

Render PostgreSQL 화면에서 `Internal Database URL`을 복사합니다.

Render Backend Web Service의 `Environment` 화면에 다음처럼 넣습니다.

```text
DATABASE_URL=복사한 Internal Database URL
```

`DATABASE_URL` 하나만 넣으면 Campio가 내부에서 다음 값으로 변환합니다.

```text
spring.datasource.url=jdbc:postgresql://HOST:5432/DB
spring.datasource.username=URL 안의 USER
spring.datasource.password=URL 안의 PASSWORD
```

## 4. 백엔드 재배포와 로그 확인

환경변수 저장 후:

1. Backend Web Service 화면으로 갑니다.
2. 오른쪽 위 `Manual Deploy`를 누릅니다.
3. `Deploy latest commit`을 누릅니다.
4. 왼쪽 메뉴 `Logs`를 엽니다.
5. 아래 내용을 확인합니다.

정상 로그 기준:

- Spring profile이 `prod`
- Flyway migration 성공
- `Started CampioApplication`
- DB password나 세션 값이 로그에 출력되지 않음

## 5. Health endpoint 테스트

Render 배포가 끝나면 브라우저 또는 터미널에서 확인합니다.

```bash
curl https://YOUR-BACKEND.onrender.com/api/health
```

정상 응답 예시:

```json
{"status":"ok","timestamp":"..."}
```

## 6. Vercel 프론트 배포

1. Vercel Dashboard로 이동합니다.
2. `Add New` -> `Project`를 누릅니다.
3. Campio GitHub 저장소를 선택합니다.
4. 설정을 아래처럼 입력합니다.

| 항목 | 값 |
| --- | --- |
| Framework Preset | `Vite` |
| Root Directory | `campio-frontend` |
| Build Command | `npm run build` |
| Output Directory | `dist` |

5. Environment Variables에 아래 값을 넣습니다.

```text
VITE_API_BASE_URL=https://YOUR-BACKEND.onrender.com
```

주의: 뒤에 `/api`를 붙이지 마세요. 프론트 API 코드가 이미 `/api/auth/login`, `/api/opportunities` 같은 path를 붙입니다.

Vercel/Vite 환경변수는 빌드 시점에 번들에 들어갑니다. 값을 바꾸면 Vercel에서 반드시 다시 Deploy해야 합니다.

## 7. Vercel 배포 후 Render CORS 수정

Vercel 배포가 끝나면 실제 프론트 URL이 생깁니다.

예시:

```text
https://campio.vercel.app
```

Render Backend Web Service의 환경변수에서 아래 값을 실제 Vercel URL로 바꿉니다.

```text
FRONTEND_ORIGIN=https://campio.vercel.app
```

저장 후 다시:

1. `Manual Deploy`
2. `Deploy latest commit`

## 8. 최종 테스트 순서

1. `https://YOUR-BACKEND.onrender.com/api/health`가 `ok`인지 확인합니다.
2. Vercel URL을 엽니다.
3. `/explore` 페이지가 열리는지 확인합니다.
4. 회원가입을 합니다.
5. 로그아웃 후 다시 로그인합니다.
6. 공고 저장/저장 취소를 시도합니다.
7. Render PostgreSQL에서 `users`, `saved_opportunities` 등에 데이터가 생겼는지 확인합니다.
8. 브라우저 새로고침 후에도 로그인 상태가 유지되는지 확인합니다.

## 9. 운영 보안 확인 결과

- prod profile에서 mock user는 `campio.auth.allow-mock-user=false`입니다.
- prod profile에서 H2 console은 꺼져 있습니다.
- seed data는 현재 코드에 없습니다. `CAMPIO_SEED_ENABLED`도 사용하지 않습니다.
- 관리자 계정은 `CAMPIO_ADMIN_EMAIL`과 `CAMPIO_ADMIN_PASSWORD`가 둘 다 있을 때만 생성됩니다.
- 비밀번호는 BCrypt로 해시 저장됩니다.
- CORS는 `FRONTEND_ORIGIN`에 입력한 origin만 허용하고 credentials를 허용합니다.
- 프론트 fetch는 `credentials: "include"`를 사용합니다.
- prod session cookie는 `Secure=true`, `SameSite=None`입니다. 서로 다른 Vercel/Render 도메인에서 쿠키 인증을 쓰기 위한 설정입니다.
- `HttpOnly`는 Spring Session cookie 기본값으로 적용됩니다.
- Swagger와 Actuator 의존성은 현재 없습니다.
- `/api/admin/ingestion/**`는 공개 URL이지만 세션의 관리자 권한을 요구합니다.

## 10. 데이터 수집 운영

배포 직후 자동 크롤링은 기본 비활성화입니다.

현재 코드에는 기본 소스 이름과 URL이 등록됩니다.

- `K-Startup 모집중 사업공고`
- `기업마당 지원사업 공고`

하지만 기본적으로 source `enabled=false`, startup auto-run `false`입니다. 실제 수집 전 관리자 검토 후 켜세요.

API/RSS/HTML Adapter별 준비값:

| Adapter | 필수 입력값 |
| --- | --- |
| API | 소스 이름, 공식 URL, API endpoint, API key 필요 여부, 요청 주기, 페이지네이션 방식, 중복 식별 기준 |
| RSS | 소스 이름, 공식 URL, RSS URL, 요청 주기, 중복 식별 기준 |
| HTML | 소스 이름, 공식 URL, robots.txt 허용 여부, 요청 주기, User-Agent, 페이지네이션 방식, 중복 식별 기준 |

사용자가 준비해야 할 정보:

- 소스 이름
- 공식 URL
- API endpoint
- API key
- RSS URL
- robots.txt 허용 여부
- 요청 주기
- User-Agent
- 페이지네이션 방식
- 중복 식별 기준
- 관리자 검수 방식

구현된 흐름:

```text
opportunity_sources -> raw_opportunities -> 관리자 검수/발행 -> opportunities
```

코드에는 raw 저장, 중복 upsert, 관리자 publish API가 구현되어 있습니다. 일부 HTML 소스는 deadline이 명확하면 자동 publish할 수 있으므로, 운영에서는 source enabled와 startup auto-run을 신중히 켜야 합니다.

### API key는 지금 필요 없음

현재 운영 배포, 회원가입/로그인, DB 저장 테스트, 기본 HTML 소스 등록에는 외부 API key가 필요하지 않습니다.

API key가 필요한 시점은 다음 중 하나입니다.

- HTML 수집 대신 공식 Open API Adapter를 추가할 때
- 공공데이터포털 API를 production source로 등록할 때
- 민간 채용/공모전/장학금 플랫폼과 제휴 API를 붙일 때
- API key가 필요한 RSS/JSON feed를 운영 source로 켤 때

그때 준비할 값:

| 항목 | 어디서 가져오는지 | Render 저장 위치 |
| --- | --- | --- |
| 공공데이터 API key | `https://www.data.go.kr` 로그인 -> 데이터찾기 -> 오픈 API 검색 -> 활용신청 -> 마이페이지/인증키 | 아직 코드 변수 없음. Adapter 추가 시 `CAMPIO_SOURCE_..._API_KEY`처럼 새 환경변수로 추가 |
| K-Startup API key | 공식 API가 공공데이터포털에 제공되는 경우 해당 API 상세 페이지에서 활용신청 | Adapter 추가 시 별도 환경변수 |
| 기업마당 API key | 공식 API가 공공데이터포털 또는 기업마당/운영기관에서 제공되는 경우 해당 API 신청 페이지 | Adapter 추가 시 별도 환경변수 |
| 민간 서비스 API key | 해당 서비스 개발자 콘솔 또는 제휴 담당자 | Adapter 추가 시 별도 환경변수 |

API key를 가져와야 하는 단계가 되면 Codex가 먼저 말해야 할 내용:

1. 어떤 소스에 API key가 필요한지
2. 정확히 어느 사이트에서 발급받는지
3. Render에 어떤 환경변수 이름으로 넣을지
4. key를 코드나 Git에 저장하지 않아도 되는지
5. 테스트 호출에 필요한 endpoint와 rate limit

## 11. 자주 발생하는 오류

### DB 연결 실패

확인할 것:

- `DATABASE_URL`이 Render PostgreSQL의 Internal Database URL인지
- URL 전체를 빠짐없이 붙여 넣었는지
- `SPRING_PROFILES_ACTIVE=prod`인지

### Flyway 오류

확인할 것:

- Render 로그에서 migration 실패 SQL 확인
- 운영 DB를 수동으로 수정하지 않았는지 확인
- 새 schema 변경은 `db/migration`에 새 migration 파일로만 추가

### CORS 오류

확인할 것:

- `FRONTEND_ORIGIN`이 실제 접속 중인 Vercel URL과 정확히 같은지
- URL 끝에 `/`가 붙어 있지 않은지
- Vercel preview URL로 테스트 중이면 그 preview URL을 임시로 `FRONTEND_ORIGIN`에 넣고 재배포했는지

### 로그인은 되는데 저장이 안 됨

확인할 것:

- 프론트 `VITE_API_BASE_URL`이 `https://YOUR-BACKEND.onrender.com` 형식인지
- `/api`를 붙이지 않았는지
- Render `FRONTEND_ORIGIN` 수정 후 백엔드를 재배포했는지
- Vercel 환경변수 수정 후 프론트를 재배포했는지

### Vercel에서 API URL을 바꿨는데 반영 안 됨

Vite 환경변수는 빌드 시점에 들어갑니다. Vercel에서 `Redeploy`를 실행하세요.

### Render 서비스가 잠깐 느림

Render free plan은 일정 시간 미사용 후 sleep될 수 있습니다. 첫 요청이 느릴 수 있습니다.
