#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [[ -f "${SCRIPT_DIR}/prod.env" ]]; then
  # shellcheck source=/dev/null
  source "${SCRIPT_DIR}/prod.env"
fi

: "${PUBLIC_HOST:?Set PUBLIC_HOST in bootstrap/prod.env or shell env.}"
: "${LETSENCRYPT_EMAIL:?Set LETSENCRYPT_EMAIL in bootstrap/prod.env or shell env.}"
: "${PUBLIC_HTTPS_PORT_IN_REDIRECT:=443}"
: "${APP_NAMESPACE:=erd-tool}"
: "${APP_TLS_SECRET:=erd-tool-prod-tls}"
: "${OPS_BASIC_AUTH_SECRET:=erd-tool-ops-basic-auth}"
: "${ARGOCD_BASIC_AUTH_SECRET:=argocd-ops-basic-auth}"

if ! command -v helm >/dev/null 2>&1; then
  echo "helm is required."
  exit 1
fi

if ! command -v kubectl >/dev/null 2>&1; then
  echo "kubectl is required."
  exit 1
fi

helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo add jetstack https://charts.jetstack.io
helm repo add argo https://argoproj.github.io/argo-helm
helm repo update

kubectl create namespace ingress-nginx --dry-run=client -o yaml | kubectl apply -f -
kubectl create namespace cert-manager --dry-run=client -o yaml | kubectl apply -f -
kubectl create namespace argocd --dry-run=client -o yaml | kubectl apply -f -
kubectl create namespace "${APP_NAMESPACE}" --dry-run=client -o yaml | kubectl apply -f -

helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx \
  --values "${SCRIPT_DIR}/ingress-nginx-values.yaml"

helm upgrade --install cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --values "${SCRIPT_DIR}/cert-manager-values.yaml"

sed "s|__LETSENCRYPT_EMAIL__|${LETSENCRYPT_EMAIL}|g" \
  "${SCRIPT_DIR}/cluster-issuer-prod.yaml" | kubectl apply -f -

helm upgrade --install argocd argo/argo-cd \
  --namespace argocd \
  --values "${SCRIPT_DIR}/argocd-values.yaml"

sed \
  -e "s|__PUBLIC_HOST__|${PUBLIC_HOST}|g" \
  -e "s|__PUBLIC_HTTPS_PORT_IN_REDIRECT__|${PUBLIC_HTTPS_PORT_IN_REDIRECT}|g" \
  -e "s|__APP_TLS_SECRET__|${APP_TLS_SECRET}|g" \
  -e "s|__ARGOCD_BASIC_AUTH_SECRET__|${ARGOCD_BASIC_AUTH_SECRET}|g" \
  "${SCRIPT_DIR}/argocd-ingress.yaml" | kubectl apply -f -

echo "Platform bootstrap completed."
echo "Next: run create-prod-secrets.sh and then apply argocd/application-prod.yaml."
