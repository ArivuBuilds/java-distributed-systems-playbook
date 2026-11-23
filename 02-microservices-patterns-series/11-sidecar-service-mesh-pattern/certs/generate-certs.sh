#!/usr/bin/env bash
set -euo pipefail

CERTS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${CERTS_DIR}"

# 1. Root Certificate Authority (CA)
openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 \
  -subj "/CN=mesh.local/O=ArivuBuilds Mesh CA" \
  -keyout ca-key.pem -out ca-cert.pem

# 2. Sidecar Ingress mTLS Key & Cert (Service Identity: order-service)
openssl req -nodes -newkey rsa:2048 \
  -subj "/CN=order-service.mesh.local/O=ArivuBuilds Mesh" \
  -keyout envoy-key.pem -out envoy-req.pem

openssl x509 -req -days 365 -set_serial 01 \
  -CA ca-cert.pem -CAkey ca-key.pem \
  -in envoy-req.pem -out envoy-cert.pem

# 3. Client Verification Cert (Identity: upstream-caller)
openssl req -nodes -newkey rsa:2048 \
  -subj "/CN=client.mesh.local/O=ArivuBuilds Caller" \
  -keyout client-key.pem -out client-req.pem

openssl x509 -req -days 365 -set_serial 02 \
  -CA ca-cert.pem -CAkey ca-key.pem \
  -in client-req.pem -out client-cert.pem

rm -f *-req.pem
echo "mTLS certificates generated under certs/"
