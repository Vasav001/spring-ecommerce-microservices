#!/usr/bin/env bash
set -euo pipefail

: "${PROJECT_ID:?set PROJECT_ID}" "${REGION:?set REGION}" "${DOMAIN:?set DOMAIN}"
TAG="${TAG:-latest}"
REPO="${REGION}-docker.pkg.dev/${PROJECT_ID}/ecom"

cd "$(dirname "$0")"
[ -f secrets.env ] || { echo "k8s/secrets.env is missing: copy secrets.env.example and fill it in"; exit 1; }

out=.rendered
rm -rf "$out" && mkdir -p "$out"
for f in [0-9]*.yaml; do
  sed -e "s#__DOMAIN__#${DOMAIN}#g" -e "s#__IMAGE_REPO__#${REPO}#g" -e "s#__TAG__#${TAG}#g" "$f" > "$out/$f"
done

grep -vE '^(KEYCLOAK_ADMIN_PASSWORD=|ADMIN1_PASSWORD=|#|\s*$)' secrets.env > "$out/app.env"
grep -E '^KEYCLOAK_ADMIN_PASSWORD=' secrets.env > "$out/keycloak.env"
trap 'rm -f "$out/app.env" "$out/keycloak.env"' EXIT

kubectl apply -f "$out/00-namespace.yaml"
kubectl -n ecom create secret generic ecom-secrets \
  --from-env-file="$out/app.env" --dry-run=client -o yaml | kubectl apply -f -
kubectl -n ecom create secret generic keycloak-admin \
  --from-env-file="$out/keycloak.env" --dry-run=client -o yaml | kubectl apply -f -
kubectl -n ecom create configmap postgres-init \
  --from-file=init.sql=../docker/postgres/init.sql --dry-run=client -o yaml | kubectl apply -f -
kubectl -n ecom create configmap keycloak-realm \
  --from-file=ecom-realm-realm.json=../docker/keycloak/ecom-realm-realm.json --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -f "$out"
bash reset-admin1.sh
