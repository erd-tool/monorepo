#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [[ -f "${SCRIPT_DIR}/prod.env" ]]; then
  # shellcheck source=/dev/null
  source "${SCRIPT_DIR}/prod.env"
fi

: "${APP_NAMESPACE:=erd-tool}"
: "${OPS_BASIC_AUTH_SECRET:=erd-tool-ops-basic-auth}"
: "${ARGOCD_BASIC_AUTH_SECRET:=argocd-ops-basic-auth}"
: "${DB_CONFIG_FILE:?Point DB_CONFIG_FILE to a db.yml file that uses the server private IP/FQDN.}"
: "${JWT_SECRET:?Provide JWT_SECRET.}"
: "${GHCR_USERNAME:?Provide GHCR_USERNAME.}"
: "${GHCR_TOKEN:?Provide GHCR_TOKEN.}"
: "${INGRESS_BASIC_AUTH_USER:?Provide INGRESS_BASIC_AUTH_USER.}"
: "${INGRESS_BASIC_AUTH_PASSWORD:?Provide INGRESS_BASIC_AUTH_PASSWORD.}"
: "${GRAFANA_ADMIN_USER:?Provide GRAFANA_ADMIN_USER.}"
: "${GRAFANA_ADMIN_PASSWORD:?Provide GRAFANA_ADMIN_PASSWORD.}"
: "${KIBANA_ENCRYPTION_KEY:?Provide KIBANA_ENCRYPTION_KEY.}"

if ! command -v htpasswd >/dev/null 2>&1; then
  echo "htpasswd is required. Install apache2-utils first."
  exit 1
fi

kubectl create namespace "${APP_NAMESPACE}" --dry-run=client -o yaml | kubectl apply -f -
kubectl create namespace argocd --dry-run=client -o yaml | kubectl apply -f -

kubectl -n "${APP_NAMESPACE}" create secret generic erd-tool-db-config \
  --from-file=db.yml="${DB_CONFIG_FILE}" \
  --dry-run=client -o yaml | kubectl apply -f -

kubectl -n "${APP_NAMESPACE}" create secret generic erd-tool-jwt \
  --from-literal=JWT_SECRET="${JWT_SECRET}" \
  --dry-run=client -o yaml | kubectl apply -f -

kubectl -n "${APP_NAMESPACE}" create secret docker-registry ghcr-pull-secret \
  --docker-server=ghcr.io \
  --docker-username="${GHCR_USERNAME}" \
  --docker-password="${GHCR_TOKEN}" \
  --docker-email="${GHCR_EMAIL:-ops@example.com}" \
  --dry-run=client -o yaml | kubectl apply -f -

kubectl -n "${APP_NAMESPACE}" create secret generic grafana-admin \
  --from-literal=GF_SECURITY_ADMIN_USER="${GRAFANA_ADMIN_USER}" \
  --from-literal=GF_SECURITY_ADMIN_PASSWORD="${GRAFANA_ADMIN_PASSWORD}" \
  --dry-run=client -o yaml | kubectl apply -f -

kubectl -n "${APP_NAMESPACE}" create secret generic kibana-secrets \
  --from-literal=XPACK_ENCRYPTEDSAVEDOBJECTS_ENCRYPTIONKEY="${KIBANA_ENCRYPTION_KEY}" \
  --dry-run=client -o yaml | kubectl apply -f -

TMP_AUTH_FILE="$(mktemp)"
trap 'rm -f "${TMP_AUTH_FILE}"' EXIT
htpasswd -bc "${TMP_AUTH_FILE}" "${INGRESS_BASIC_AUTH_USER}" "${INGRESS_BASIC_AUTH_PASSWORD}"

kubectl -n "${APP_NAMESPACE}" create secret generic "${OPS_BASIC_AUTH_SECRET}" \
  --from-file=auth="${TMP_AUTH_FILE}" \
  --dry-run=client -o yaml | kubectl apply -f -

kubectl -n argocd create secret generic "${ARGOCD_BASIC_AUTH_SECRET}" \
  --from-file=auth="${TMP_AUTH_FILE}" \
  --dry-run=client -o yaml | kubectl apply -f -

echo "Secrets created."
echo "ArgoCD initial admin password:"
echo "kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath='{.data.password}' | base64 -d && echo"
