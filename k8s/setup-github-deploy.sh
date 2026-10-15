#!/usr/bin/env bash
set -euo pipefail
PROJECT_ID="${PROJECT_ID:-ecom-511712}"
REPO="${REPO:-Vasav001/spring-ecommerce-microservices}"
SA="github-deploy@${PROJECT_ID}.iam.gserviceaccount.com"
NUMBER=$(gcloud projects describe "$PROJECT_ID" --format='value(projectNumber)')

gcloud services enable iamcredentials.googleapis.com sts.googleapis.com --project "$PROJECT_ID"

gcloud iam workload-identity-pools describe github --location=global --project "$PROJECT_ID" >/dev/null 2>&1 ||
  gcloud iam workload-identity-pools create github --location=global --project "$PROJECT_ID" --display-name=github

gcloud iam workload-identity-pools providers describe github --workload-identity-pool=github --location=global --project "$PROJECT_ID" >/dev/null 2>&1 ||
  gcloud iam workload-identity-pools providers create-oidc github --workload-identity-pool=github --location=global --project "$PROJECT_ID" \
    --issuer-uri=https://token.actions.githubusercontent.com \
    --attribute-mapping=google.subject=assertion.sub,attribute.repository=assertion.repository,attribute.ref=assertion.ref \
    --attribute-condition="assertion.repository=='${REPO}' && assertion.ref=='refs/heads/main'"

gcloud iam service-accounts describe "$SA" --project "$PROJECT_ID" >/dev/null 2>&1 ||
  gcloud iam service-accounts create github-deploy --project "$PROJECT_ID" --display-name="GitHub deploy"

for role in roles/artifactregistry.writer roles/container.developer; do
  gcloud projects add-iam-policy-binding "$PROJECT_ID" --member="serviceAccount:${SA}" --role="$role" --condition=None >/dev/null
done

gcloud iam service-accounts add-iam-policy-binding "$SA" --project "$PROJECT_ID" --role=roles/iam.workloadIdentityUser \
  --member="principalSet://iam.googleapis.com/projects/${NUMBER}/locations/global/workloadIdentityPools/github/attribute.repository/${REPO}" >/dev/null

echo "done: GitHub Actions on ${REPO}@main can now push images and deploy as ${SA}"
