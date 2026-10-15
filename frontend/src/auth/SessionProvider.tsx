import { useMemo, useState, type ReactNode } from 'react'
import { AuthProvider, useAuth } from 'react-oidc-context'
import { setAccessToken } from '../api/client'
import { rolesFromToken, SessionContext, type Role, type Session } from './session'

const fakeAuth = import.meta.env.VITE_FAKE_AUTH === 'true'
const FAKE_ROLE_KEY = 'fakeRole'

const oidcConfig = {
  authority: import.meta.env.VITE_KEYCLOAK_URL,
  client_id: import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
  redirect_uri: `${window.location.origin}/`,
  post_logout_redirect_uri: `${window.location.origin}/`,
  automaticSilentRenew: true,
  onSigninCallback: () => window.history.replaceState({}, document.title, window.location.pathname),
}

export function SessionProvider({ children }: { children: ReactNode }) {
  if (fakeAuth) {
    return <FakeSession>{children}</FakeSession>
  }
  return (
    <AuthProvider {...oidcConfig}>
      <OidcSession>{children}</OidcSession>
    </AuthProvider>
  )
}

function OidcSession({ children }: { children: ReactNode }) {
  const auth = useAuth()
  const token = auth.user?.access_token
  setAccessToken(token)
  const value = useMemo<Session>(() => {
    const roles = rolesFromToken(token)
    return {
      status: auth.isLoading ? 'loading' : auth.isAuthenticated ? 'authenticated' : 'anonymous',
      name: auth.user?.profile.preferred_username,
      roles,
      isAdmin: roles.includes('ADMIN'),
      login: () => void auth.signinRedirect(),
      logout: () => void auth.signoutRedirect(),
      fake: false,
    }
  }, [auth, token])
  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>
}

function readFakeRole(): Role | null {
  try {
    const role = localStorage.getItem(FAKE_ROLE_KEY)
    return role === 'CUSTOMER' || role === 'ADMIN' ? role : null
  } catch {
    return null
  }
}

function writeFakeRole(role: Role | null) {
  try {
    if (role) {
      localStorage.setItem(FAKE_ROLE_KEY, role)
    } else {
      localStorage.removeItem(FAKE_ROLE_KEY)
    }
  } catch {
    return
  }
}

function FakeSession({ children }: { children: ReactNode }) {
  const [role, setRole] = useState<Role | null>(readFakeRole)
  setAccessToken('mock-token')
  const value = useMemo<Session>(() => {
    const roles = role === 'ADMIN' ? ['CUSTOMER', 'ADMIN'] : role === 'CUSTOMER' ? ['CUSTOMER'] : []
    return {
      status: role ? 'authenticated' : 'anonymous',
      name: role ? `mock-${role.toLowerCase()}` : undefined,
      roles,
      isAdmin: role === 'ADMIN',
      login: (next: Role = 'CUSTOMER') => {
        writeFakeRole(next)
        setRole(next)
      },
      logout: () => {
        writeFakeRole(null)
        setRole(null)
      },
      fake: true,
    }
  }, [role])
  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>
}
