# erd-tool-infra-template

이 디렉터리는 별도 GitOps 저장소 예시인 `erd-tool-infra` 로 분리해 옮길 템플릿입니다.

구성은 다음처럼 나눴습니다.

- `charts/erd-tool`
  - 앱 + observability 스택을 단일 서버 k3s에 배포하는 umbrella Helm chart
- `environments/prod/values.yaml`
  - 운영 host, 이미지 태그, replica, PVC 크기, 외부 포트
- `argocd/application-prod.yaml`
  - ArgoCD가 감시할 prod Application
- `bootstrap`
  - k3s, ingress-nginx, cert-manager, ArgoCD 초기 설치와 secret 생성 스크립트

## 이 템플릿이 가정하는 운영 구조

- 클러스터: 단일 서버 `k3s`
- Ingress: `ingress-nginx`
- TLS: `cert-manager` + Let’s Encrypt
- GitOps: ArgoCD auto-sync
- DB: Linux 서버에서 Docker로 계속 운영 중인 PostgreSQL
- 앱 DB 연결: Kubernetes Secret으로 주입되는 `/config/db.yml`

공유기 정책 때문에 공인 `80/443`을 직접 열 수 없다면 `2010/2043` 같은 외부 포트를 쓰고, 서버 내부에서는 `80/443` ingress로 릴레이하는 구성이 현실적입니다. 이 경우 `global.externalPort`를 실제 사용자 HTTPS 포트와 맞춰야 Grafana/Kibana가 올바른 URL을 생성합니다.

## 처음 옮긴 뒤 가장 먼저 바꿀 값

- `environments/prod/values.yaml`
  - `global.host`
  - `global.externalPort`
  - `global.imageRepository`
  - `global.imageTag`
- `argocd/application-prod.yaml`
  - `spec.source.repoURL`
  - `spec.source.targetRevision`
- `bootstrap/prod.env.example`
  - 실제 도메인, 이메일, secret 이름 기준으로 복사해서 사용

## GitHub Actions와의 연결

앱 repo의 `.github/workflows/cd-prod.yml` 은 이 repo의 `environments/prod/values.yaml` 안 `global.imageTag` 를 갱신하는 방식으로 동작합니다.

즉, 앱 repo는 "새 이미지를 만든다", infra repo는 "이번 수업에 어느 버전을 올릴지 배치한다", ArgoCD는 "배치도대로 실제 강의실을 정리한다" 라고 보면 됩니다.
