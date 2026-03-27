# ERD Collaboration Tool

ERD Cloud 스타일의 웹 기반 협업 ERD 도구 초기 구현입니다.

## Stack

- Backend: `Java 21`, `Spring Boot`, `Spring Security`, `Spring Data JPA`, `Gradle`
- Frontend: `React`, `TypeScript`, `Vite`, `@xyflow/react`
- Realtime: `Yjs`, `y-websocket`
- Infra: `Docker`, `docker-compose`, `PostgreSQL`, `Redis`

## Run

```bash
cp .env.example .env
docker compose build
docker compose up
```

기본 접속:

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- Collaboration: `ws://localhost:1234`

## Git Convention

브랜치명:

- `feature/auth-login-signup`
- `feature/erd-editor-core`
- `feature/team-invitation`
- `fix/realtime-sync-conflict`
- `chore/docker-compose-bootstrap`

커밋 메시지:

- `feat: 회원가입 및 로그인 API 추가`
- `feat: ERD 엔티티 및 관계 편집 기능 구현`
- `fix: 실시간 동기화 충돌 수정`
- `chore: docker-compose 운영 환경 추가`
- `docs: README 실행 가이드 정리`

## Current Scope

- 회원가입 / 로그인 / 내 정보 조회
- 팀 생성 / 조회 / 초대 / 수락
- ERD 생성 / 조회 / 수정 / 삭제
- React Flow 기반 ERD 편집
- Yjs 기반 실시간 동기화
- 자동 저장 / undo / redo
- SQL export / PNG export

