# Kubernetes + GitOps 전환 가이드

> [!IMPORTANT]
> 이 문서는 reference-only monorepo 기준으로 남겨둔 전환 기록입니다.
> 현재 canonical entrypoint 는 [`erd-tool-main`](https://github.com/erd-tool/erd-tool-main) 이며, 실제 변경은 연결된 sub repo에서 진행합니다.

이 저장소는 이제 두 축으로 나뉩니다.

- 앱 repo: Java/Spring, React, collaboration 서비스 코드와 이미지 빌드
- infra repo: k3s 위에 올라갈 Helm chart, prod values, ArgoCD Application

수업에서 비유하면, 앱 repo는 "수업 자료 원본"이고 infra repo는 "실제 강의실 배치도"입니다. 앱이 바뀌면 배치도에서 어떤 버전을 쓸지만 바꾸고, 실제 배치는 ArgoCD가 맡습니다.

## 앱 repo에서 바뀐 점

- [frontend/nginx.conf](/Users/baekdonghyeon/my_playground/erd_tool/frontend/nginx.conf)
  - `/api`, `/ws/collaboration` 프록시를 제거했습니다.
  - ingress-nginx가 외부 라우팅을 맡고, 프론트 컨테이너는 정적 파일만 서빙합니다.
- [.github/workflows/cd-prod.yml](/Users/baekdonghyeon/my_playground/erd_tool/.github/workflows/cd-prod.yml)
  - 기존 SSH + docker compose 재기동 흐름을 제거했습니다.
  - `main` 반영 시 GHCR 이미지를 빌드하고, 별도 infra repo의 `global.imageTag`를 새 SHA로 갱신합니다.

## GitHub Actions 준비물

필수 GitHub Secret:

- `INFRA_REPO_TOKEN`
  - infra repo에 push 가능한 fine-grained PAT 또는 GitHub App token

필수 GitHub Variable:

- `INFRA_REPO`
  - 예: `erd-tool/erd-tool-infra`

선택 GitHub Variable:

- `INFRA_REPO_REF`
  - 기본값 `main`
- `INFRA_VALUES_FILE`
  - 기본값 `environments/prod/values.yaml`

## Infra Repo 구조

현재 저장소의 [infra/erd-tool-infra-template](/Users/baekdonghyeon/my_playground/erd_tool/infra/erd-tool-infra-template) 는 별도 infra repo로 옮기기 위한 템플릿입니다.

- `charts/erd-tool`
  - 앱 + observability 스택용 umbrella Helm chart
- `environments/prod/values.yaml`
  - 운영 host, 이미지 태그, PVC 크기, replica 수
- `argocd/application-prod.yaml`
  - auto-sync / prune / self-heal 포함
- `bootstrap`
  - k3s 설치, 플랫폼 초기화, 수동 secret 생성 스크립트

## 외부 포트가 80/443이 아닐 때

공유기나 IDC 정책 때문에 공인 `80/443`을 못 쓰고 `2002~2050` 같은 범위만 쓸 수 있다면, 사용자 진입 포트와 클러스터 ingress 포트를 분리해서 생각하는 게 이해하기 쉽습니다.

- 사용자 진입 포트
  - 예: `http://playdata4.iptime.org:2010`
  - 예: `https://playdata4.iptime.org:2043`
- 클러스터 내부 ingress
  - 여전히 `80/443`
- 서버 역할
  - `2010 -> 80`
  - `2043 -> 443`

Spring으로 비유하면 외부 컨트롤러 URL과 내부 서비스 포트를 어댑터 하나로 분리하는 셈입니다.

이 시나리오에서는 Helm values에 아래 값을 같이 넣어야 합니다.

- `global.domainScheme: https`
- `global.externalPort: 2043`

그래야 Grafana/Kibana가 `https://<host>:2043/...` 기준으로 리다이렉트와 절대 URL을 계산합니다.

주의할 점:

- Let’s Encrypt `HTTP-01`은 공인 `80`
- Let’s Encrypt `TLS-ALPN-01`은 공인 `443`

를 요구합니다. 따라서 공인 80/443이 막혀 있으면 기본 `cert-manager + Let’s Encrypt` 조합은 실패합니다. 이때 선택지는 둘입니다.

- DNS 제공업체 API를 쓸 수 있으면 `DNS-01`로 전환
- 아니면 인증서 자동 발급은 잠시 보류하고 비표준 HTTPS 포트에서 접속만 먼저 연다

## 운영 시크릿 원칙

Git에 넣지 않는 값:

- `db.yml`
- `JWT_SECRET`
- GHCR pull secret
- ingress basic auth
- Grafana admin 계정
- Kibana encryption key
- ArgoCD 운영 계정/초기 비밀번호

## 외부 DB 연결 원칙

Spring 관점에서 보면 `application.yml` 안 datasource 값을 직접 고치지 않고, 지금처럼 외부 설정 파일을 주입하는 구조를 유지한 것입니다.

- `auth`, `team`, `erd`는 `/config/db.yml`을 Secret으로 마운트합니다.
- JDBC URL은 반드시 리눅스 서버의 사설 IP 또는 고정 DNS를 사용합니다.
- `localhost`, `docker-compose` 서비스명, Docker 브리지 주소는 Kubernetes Pod 기준으로 모두 잘못된 대상입니다.

## 배포 성공 기준

기존:

- SSH 접속
- `docker compose up -d`
- 서버에서 curl

변경 후:

- GHCR 이미지 push 성공
- infra repo promotion commit 생성
- ArgoCD sync 완료
- ingress 경로 헬스체크 성공

## 권장 검증 순서

1. `kubectl get pods -n ingress-nginx`
2. `kubectl get certificate -A`
3. `kubectl get applications -n argocd`
4. `kubectl get ingress -n erd-tool`
5. `curl -I https://<host>/`
6. `curl -I https://<host>/healthz`
7. WebSocket 경로 `/ws/collaboration/<room-id>` 업그레이드 확인
8. `https://<host>/grafana`, `https://<host>/kibana`, `https://<host>/argocd` basic auth + native auth 확인
