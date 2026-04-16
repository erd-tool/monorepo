#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [[ -f "${SCRIPT_DIR}/prod.env" ]]; then
  # shellcheck source=/dev/null
  source "${SCRIPT_DIR}/prod.env"
fi

: "${PUBLIC_HTTP_PORT:=2010}"
: "${PUBLIC_HTTPS_PORT:=2043}"
: "${TARGET_HTTP_PORT:=80}"
: "${TARGET_HTTPS_PORT:=443}"

if ! command -v systemctl >/dev/null 2>&1; then
  echo "systemctl is required."
  exit 1
fi

if [[ ! -x /lib/systemd/systemd-socket-proxyd ]]; then
  echo "systemd-socket-proxyd is required at /lib/systemd/systemd-socket-proxyd."
  exit 1
fi

sudo tee /etc/systemd/system/erd-tool-k8s-http.socket >/dev/null <<EOF
[Unit]
Description=ERD Tool Kubernetes HTTP relay socket

[Socket]
ListenStream=0.0.0.0:${PUBLIC_HTTP_PORT}
NoDelay=true
ReusePort=true

[Install]
WantedBy=sockets.target
EOF

sudo tee /etc/systemd/system/erd-tool-k8s-http.service >/dev/null <<EOF
[Unit]
Description=ERD Tool Kubernetes HTTP relay service
After=network-online.target k3s.service
Wants=network-online.target k3s.service

[Service]
ExecStart=/lib/systemd/systemd-socket-proxyd 127.0.0.1:${TARGET_HTTP_PORT}
PrivateTmp=true
EOF

sudo tee /etc/systemd/system/erd-tool-k8s-https.socket >/dev/null <<EOF
[Unit]
Description=ERD Tool Kubernetes HTTPS relay socket

[Socket]
ListenStream=0.0.0.0:${PUBLIC_HTTPS_PORT}
NoDelay=true
ReusePort=true

[Install]
WantedBy=sockets.target
EOF

sudo tee /etc/systemd/system/erd-tool-k8s-https.service >/dev/null <<EOF
[Unit]
Description=ERD Tool Kubernetes HTTPS relay service
After=network-online.target k3s.service
Wants=network-online.target k3s.service

[Service]
ExecStart=/lib/systemd/systemd-socket-proxyd 127.0.0.1:${TARGET_HTTPS_PORT}
PrivateTmp=true
EOF

sudo systemctl daemon-reload
sudo systemctl enable --now erd-tool-k8s-http.socket erd-tool-k8s-https.socket

echo "Port relay installed."
echo "HTTP  ${PUBLIC_HTTP_PORT} -> ${TARGET_HTTP_PORT}"
echo "HTTPS ${PUBLIC_HTTPS_PORT} -> ${TARGET_HTTPS_PORT}"
