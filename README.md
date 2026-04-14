# ERD Collaboration Tool

ERD Cloud 스타일의 웹 기반 협업 ERD 도구입니다. 현재는 `gateway-service`, `auth-service`, `team-service`, `erd-service`, `collaboration`로 나뉜 소형 MSA 구조와 observability 스택까지 포함합니다.

## Stack

- Backend: `Java 21`, `Spring Boot`, `Spring Security`, `Spring Data JPA`, `Spring HATEOAS`, `Gradle`
- Frontend: `React`, `TypeScript`, `Vite`, `@xyflow/react`
- Realtime: `Yjs`, `y-websocket`
- Infra: `Docker`, `docker-compose`, `PostgreSQL`, `Redis`
- Metrics: `Spring Boot Actuator`, `Micrometer`, `Prometheus`, `Grafana`
- Logs: `ELK (Elasticsearch, Logstash, Kibana)`

## Architecture

- `gateway-service`
  - 외부 진입점
  - JWT 검증, request correlation, API/WebSocket 라우팅
- `auth-service`
  - 회원가입, 로그인, 내 정보 조회
- `team-service`
  - 팀 생성, 조회, 초대, 수락/거절
- `erd-service`
  - ERD CRUD, 공개 ERD 조회, SQL export
- `collaboration`
  - Yjs WebSocket 기반 실시간 협업
- `postgres`
  - `auth`, `team`, `erd` 스키마 저장소
- `prometheus`, `grafana`
  - 메트릭 수집 및 시각화
- `elasticsearch`, `logstash`, `kibana`
  - JSON 로그 수집, 적재, 검색

## Run

```bash
cp .env.example .env
docker compose build
docker compose up -d
```

기본 접속:

- Frontend: `http://localhost:5173`
- Gateway API: `http://localhost:8080`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- Elasticsearch: `http://localhost:9200`
- Kibana: `http://localhost:5601`

기본 운영 엔드포인트:

- Gateway health: `http://localhost:8080/actuator/health`
- Gateway metrics: `http://localhost:8080/actuator/prometheus`
- Collaboration metrics: 내부 경로 `http://collaboration:1234/metrics`

Grafana 기본 계정:

- ID: `admin`
- PW: `admin`

## HATEOAS / Observability

- 외부 공개 REST 응답은 HATEOAS `_links`를 포함합니다.
- 내부 `/internal/**` API 계약은 유지합니다.
- Spring 서비스는 `/actuator/health`, `/actuator/info`, `/actuator/prometheus`를 노출합니다.
- `collaboration` 서비스는 `/healthz`, `/metrics`를 제공합니다.
- 로그는 JSON stdout으로 출력되며 docker logging driver를 통해 Logstash로 전달됩니다.
- Prometheus는 `gateway-service`, `auth-service`, `team-service`, `erd-service`, `collaboration`를 scrape합니다.

## Verification

기본 검증 순서:

```bash
docker compose ps
curl http://localhost:8080/actuator/health
curl http://localhost:9090/-/healthy
curl http://localhost:3000/api/health
curl http://localhost:5601/api/status
```

Prometheus target 확인:

```bash
docker compose exec -T prometheus wget -qO- http://127.0.0.1:9090/api/v1/targets
```

ELK 적재 확인:

```bash
curl http://localhost:9200/_cat/indices?v
```

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
- HAL `_links` 기반 HATEOAS 응답
- React Flow 기반 ERD 편집
- Yjs 기반 실시간 동기화
- 자동 저장 / undo / redo
- SQL export / PNG export
- Prometheus / Grafana 메트릭 모니터링
- ELK 기반 로그 수집 및 검색
