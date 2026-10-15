#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"
value() { grep -E "^$1=" secrets.env | head -1 | cut -d= -f2- | tr -d '\r'; }
KC_ADMIN_PASSWORD="$(value KEYCLOAK_ADMIN_PASSWORD)"
ADMIN1_PASSWORD="$(value ADMIN1_PASSWORD)"
[ -n "$KC_ADMIN_PASSWORD" ] && [ -n "$ADMIN1_PASSWORD" ] || { echo "KEYCLOAK_ADMIN_PASSWORD and ADMIN1_PASSWORD must be set in k8s/secrets.env"; exit 1; }

PORT=18080
BASE="http://localhost:${PORT}/auth"
kubectl -n ecom rollout status deploy/keycloak --timeout=10m >/dev/null
kubectl -n ecom port-forward svc/keycloak "${PORT}:8080" >/dev/null 2>&1 &
pf=$!
trap 'kill "$pf" 2>/dev/null || true' EXIT
for _ in $(seq 1 30); do curl -sf "${BASE}/realms/master" >/dev/null && break; sleep 2; done

token=$(curl -s -X POST "${BASE}/realms/master/protocol/openid-connect/token" \
  -d grant_type=password -d client_id=admin-cli -d username=admin --data-urlencode "password=${KC_ADMIN_PASSWORD}" \
  | sed -nE 's/.*"access_token":"([^"]+)".*/\1/p')
[ -n "$token" ] || { echo "could not log in to Keycloak as the master admin"; exit 1; }

id=$(curl -s "${BASE}/admin/realms/ecom-realm/users?username=admin1&exact=true" -H "Authorization: Bearer ${token}" \
  | grep -oE '"id":"[^"]+"' | head -1 | cut -d'"' -f4)
[ -n "$id" ] || { echo "admin1 not found in ecom-realm"; exit 1; }

escaped=$(printf %s "$ADMIN1_PASSWORD" | sed 's/\\/\\\\/g; s/"/\\"/g')
body=$(printf '{"type":"password","temporary":false,"value":"%s"}' "$escaped")
code=$(curl -s -o /dev/null -w '%{http_code}' -X PUT "${BASE}/admin/realms/ecom-realm/users/${id}/reset-password" \
  -H "Authorization: Bearer ${token}" -H 'Content-Type: application/json' -d "$body")
[ "$code" = 204 ] || { echo "password reset failed (HTTP ${code})"; exit 1; }
echo "admin1 password set from k8s/secrets.env"
