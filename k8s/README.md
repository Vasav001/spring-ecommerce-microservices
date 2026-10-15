# Deploying to GKE

One zonal Standard cluster with a single `e2-standard-2` node runs everything. Kafka stays on Confluent Cloud.

```
https://<domain>/          frontend
https://<domain>/api/...   services
https://<domain>/auth/...  Keycloak
```

## 1. One-time setup

```bash
gcloud auth login
gcloud config set project <PROJECT_ID>
gcloud services enable container.googleapis.com artifactregistry.googleapis.com cloudbuild.googleapis.com
gcloud artifacts repositories create ecom --repository-format=docker --location=<REGION>
gcloud compute addresses create ecom-ip --global
gcloud compute addresses describe ecom-ip --global --format='value(address)'
```

Point your domain's DNS `A` record at that IP. Without a domain, use `<ip-with-dashes>.sslip.io` (for `34.1.2.3` use `34-1-2-3.sslip.io`).

## 2. Build and push the images (runs in Cloud Build, not on your machine)

```bash
gcloud builds submit --config cloudbuild.yaml --substitutions=_REGION=<REGION>,_TAG=v1,_PUBLIC_URL=https://<DOMAIN>
```

## 3. Connect kubectl

```bash
gcloud components install gke-gcloud-auth-plugin
gcloud container clusters get-credentials <CLUSTER> --zone <ZONE>
```

## 4. Secrets

Copy `k8s/secrets.env.example` to `k8s/secrets.env` (gitignored) and fill it in. Use strong values for `DB_PASSWORD`, `KEYCLOAK_ADMIN_PASSWORD` (Keycloak's master `admin`) and `ADMIN1_PASSWORD` (the shop admin `admin1`).

## 5. Deploy

```bash
PROJECT_ID=<PROJECT_ID> REGION=<REGION> DOMAIN=<DOMAIN> TAG=v1 ./k8s/deploy.sh
```

## 6. Watch it come up

```bash
kubectl -n ecom get pods -w
kubectl -n ecom describe managedcertificate ecom-cert
kubectl -n ecom get ingress ecom
```

Services restart a few times until Postgres and Keycloak are ready; that is expected. The certificate turns `Active` 15-60 minutes after DNS points at the IP. Then open `https://<DOMAIN>`.

`deploy.sh` ends by running `reset-admin1.sh`, which sets `admin1`'s password to `ADMIN1_PASSWORD` through a private `kubectl port-forward`, so the dev password from the realm file never stays active. Run `./k8s/reset-admin1.sh` on its own to change it later.

## Updating

Rebuild with a new tag, then rerun `deploy.sh` with that `TAG`.

## Troubleshooting

- `ImagePullBackOff`: give the node service account read access to Artifact Registry:
  `gcloud projects add-iam-policy-binding <PROJECT_ID> --member=serviceAccount:<PROJECT_NUMBER>-compute@developer.gserviceaccount.com --role=roles/artifactregistry.reader`
- Logs: `kubectl -n ecom logs deploy/<name>`

## Stopping costs

```bash
gcloud container clusters delete <CLUSTER> --zone <ZONE>
gcloud compute addresses delete ecom-ip --global
```

Deleting the cluster ends Postgres; its disk can be left behind, so check Compute Engine → Disks and delete any leftover `pvc-...` disk.
