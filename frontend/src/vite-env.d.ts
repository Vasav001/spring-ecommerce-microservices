/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_FAKE_AUTH: string
  readonly VITE_KEYCLOAK_URL: string
  readonly VITE_KEYCLOAK_CLIENT_ID: string
  readonly VITE_MOCK_UPLOADS: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
