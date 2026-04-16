# Bootstrap

초기 부트스트랩 순서는 아래를 기준으로 잡았습니다.

1. `install-k3s.sh`
2. `install-platform.sh`
3. `install-port-relay.sh` (공인 80/443 대신 2002~2050 같은 비표준 외부 포트를 써야 할 때만)
4. `create-prod-secrets.sh`
5. `kubectl apply -f ../argocd/application-prod.yaml`

## 왜 순서를 나누는가

Spring Boot에서 `application.yml` 과 `bootstrap` 단계가 다르듯, 클러스터도 "런타임" 과 "런타임을 올릴 플랫폼" 을 분리해야 안정적입니다.

- k3s 설치
  - Kubernetes 자체 확보
- 플랫폼 설치
  - ingress-nginx, cert-manager, ArgoCD
- 외부 포트 릴레이
  - 공유기가 80/443 포워딩을 못 할 때 `2010 -> 80`, `2043 -> 443` 같은 연결을 서버에서 고정
- 운영 secret 생성
  - DB, JWT, GHCR pull secret, basic auth
- 애플리케이션 배포
  - ArgoCD가 Helm chart를 sync

## 비표준 외부 포트를 쓰는 경우

예를 들어 공유기에서 `2002~2050`만 열 수 있으면 아래처럼 잡는 편이 안정적입니다.

- 외부 HTTP: `2010`
- 외부 HTTPS: `2043`
- 서버 내부 연결:
  - `2010 -> 80`
  - `2043 -> 443`

이때 Helm values의 `global.externalPort`는 `2043`으로 맞춰야 Grafana/Kibana가 올바른 절대 URL을 생성합니다.

또한 `install-port-relay.sh`와 함께 ingress-nginx 쪽에서도 커스텀 리다이렉트를 써야 하므로, 이 템플릿은 `allowSnippetAnnotations: true`와 `annotations-risk-level: Critical`를 켜도록 잡아두었습니다. 단일 서버, 단일 팀 운영 전제를 둔 설정입니다.

주의할 점도 있습니다.

- Let’s Encrypt `HTTP-01`은 공인 `80`
- Let’s Encrypt `TLS-ALPN-01`은 공인 `443`

를 요구하므로, 공인 80/443이 막혀 있으면 기본 설정만으로는 인증서 발급이 되지 않습니다. 이 경우는 둘 중 하나로 가야 합니다.

- DNS API를 쓸 수 있을 때: `DNS-01`로 전환
- 당장 접속만 먼저 열 때: 비표준 HTTPS 포트 + 자체 인증서 또는 브라우저 경고 감수
