#!/usr/bin/env bash

set -euo pipefail

if [[ "${EUID}" -ne 0 ]]; then
  echo "Run as root or with sudo."
  exit 1
fi

echo "[1/4] Installing k3s without Traefik"
curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC="server --disable traefik" sh -

echo "[2/4] Preparing kubeconfig access"
mkdir -p /root/.kube
cp /etc/rancher/k3s/k3s.yaml /root/.kube/config
chmod 600 /root/.kube/config

echo "[3/4] Setting vm.max_map_count for Elasticsearch"
cat >/etc/sysctl.d/99-erd-tool-k8s.conf <<'EOF'
vm.max_map_count=262144
EOF
sysctl --system

echo "[4/4] k3s bootstrap completed"
echo "Copy /etc/rancher/k3s/k3s.yaml to your operator machine if needed."
