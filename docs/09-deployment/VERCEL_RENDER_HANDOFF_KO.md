# Campio Vercel + Render 배포 핸드오프

이 문서는 Campio를 실제 배포할 때 따라 할 순서만 정리한 문서입니다.

- 프론트엔드: Vercel
- 백엔드: Render Web Service
- 데이터베이스: Render PostgreSQL

## 현재 준비 완료된 것

- 프론트 Vercel 설정: `campio-frontend/vercel.json`
- 백엔드 Dockerfile: `campio-backend/Dockerfile`
- Render Blueprint: `render.yaml`
- 운영 DB 마이그레이션: `campio-backend/src/main/resources/db/migration/V1__initial_schema.sql`
- 실제 데이터 수집 옵션은 기본 비활성화
- K-Startup, 기업마당 실제 데이터 수집기
- mock 데이터 제거

## 1. Render 배포

Render에서 새 Blueprint를 생성하고 이 저장소를 연결합니다.

Render가 읽을 파일:

```text
render.yaml
```

생성되는 리소스:

```text
campio-backend
campio-postgres
```

Render에서 입력해야 할 값:

```text
DATABASE_URL=Render PostgreSQL Internal Database URL
FRONTEND_ORIGIN=https://나중에-생길-vercel-url.vercel.app
CAMPIO_ADMIN_EMAIL=관리자이메일
CAMPIO_ADMIN_PASSWORD=강한비밀번호
```

처음에는 Vercel URL이 아직 없으므로 임시로 아래처럼 넣어도 됩니다.

```text
FRONTEND_ORIGIN=https://temp.vercel.app
```

Vercel 배포 후 실제 URL로 다시 바꾸면 됩니다.

## 2. Render 배포 확인

Render 배포가 끝나면 백엔드 URL이 생깁니다.

예시:

```text
https://campio-backend.onrender.com
```

확인:

```bash
curl https://campio-backend.onrender.com/api/health
curl https://campio-backend.onrender.com/api/opportunities
```

정상 결과:

- `/api/health`가 `status: ok` 반환
- `/api/opportunities`에 실제 공고 데이터 반환

## 3. Vercel 배포

Vercel에서 새 프로젝트를 만들고 같은 저장소를 연결합니다.

설정:

```text
Framework Preset: Vite
Root Directory: campio-frontend
Build Command: npm run build
Output Directory: dist
```

환경변수:

```text
VITE_API_BASE_URL=https://너의-render-backend-url.onrender.com
```

예시:

```text
VITE_API_BASE_URL=https://campio-backend.onrender.com
```

## 4. Render CORS 최종 수정

Vercel 배포가 끝나면 프론트 URL이 생깁니다.

예시:

```text
https://campiokr.vercel.app
```

Render 백엔드 환경변수에서 아래 값을 실제 Vercel URL로 수정합니다.

```text
FRONTEND_ORIGIN=https://campiokr.vercel.app
```

수정 후 Render 백엔드를 다시 Deploy합니다.

## 5. 운영 환경변수 최종 목록

Render 백엔드:

```text
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=Render PostgreSQL Internal Database URL
FRONTEND_ORIGIN=https://너의-vercel-url.vercel.app
CAMPIO_ADMIN_EMAIL=관리자이메일
CAMPIO_ADMIN_PASSWORD=강한비밀번호
CAMPIO_INGESTION_BOOTSTRAP_SOURCES_ENABLED=false
CAMPIO_INGESTION_AUTO_RUN_ON_STARTUP=false
CAMPIO_INGESTION_AUTO_RUN_ONLY_WHEN_EMPTY=true
```

Vercel 프론트엔드:

```text
VITE_API_BASE_URL=https://너의-render-backend-url.onrender.com
```

## 6. 배포 후 확인할 것

브라우저에서 확인:

- Vercel URL 접속
- `/explore` 접속
- 공고 카드 클릭해서 상세 페이지 열기
- 회원가입
- 로그인
- 공고 저장/저장 취소
- 새로고침해도 라우트가 유지되는지 확인

API 확인:

```bash
curl https://너의-render-backend-url.onrender.com/api/health
curl https://너의-render-backend-url.onrender.com/api/opportunities
```

## 7. 데이터 수집 동작

첫 Render 배포 후 백엔드가 켜져도 기본값으로는 자동 수집하지 않습니다.

현재 자동 수집 소스:

- K-Startup 모집중 사업공고
- 기업마당 지원사업 공고

자동 수집을 의도적으로 켜려면:

```text
CAMPIO_INGESTION_BOOTSTRAP_SOURCES_ENABLED=true
CAMPIO_INGESTION_AUTO_RUN_ON_STARTUP=true
CAMPIO_INGESTION_AUTO_RUN_ONLY_WHEN_EMPTY=true
```

의미:

- 서버 첫 실행 시 활성화된 소스만 자동 수집
- 이미 공개 공고 데이터가 있으면 재시작 때는 자동 수집 스킵
- 상시접수처럼 마감일이 애매한 데이터는 공개하지 않고 raw에만 보관

## 8. 자주 생길 수 있는 문제

### 프론트에서 API 요청 실패

확인할 것:

```text
Vercel VITE_API_BASE_URL
Render FRONTEND_ORIGIN
```

`FRONTEND_ORIGIN`은 정확한 Vercel URL이어야 합니다.

### 로그인은 되는데 저장이 안 됨

대부분 쿠키/CORS 문제입니다.

확인:

- Render `FRONTEND_ORIGIN`
- Vercel 도메인과 실제 접속 도메인이 같은지
- 백엔드가 `prod` 프로필로 실행 중인지

### Render에서 DB 연결 실패

확인:

- `DATABASE_URL`

수동 Web Service에서는 Render PostgreSQL의 Internal Database URL을 `DATABASE_URL`에 넣습니다.

### 데이터가 비어 있음

확인:

```text
CAMPIO_INGESTION_BOOTSTRAP_SOURCES_ENABLED=true
CAMPIO_INGESTION_AUTO_RUN_ON_STARTUP=true
CAMPIO_INGESTION_AUTO_RUN_ONLY_WHEN_EMPTY=true
```

기본값은 자동 수집 비활성화입니다. 실제 소스를 검토한 뒤 관리자 화면/API에서 소스를 활성화하고 crawl job을 실행하세요.

## 9. 참고 문서

- 상세 배포 문서: `docs/09-deployment/DEPLOYMENT.md`
- Vercel + Render 기술 문서: `docs/09-deployment/VERCEL_RENDER_DEPLOY.md`
- 데이터 수집 전략: `docs/10-data-ingestion/CRAWLING_STRATEGY.md`
