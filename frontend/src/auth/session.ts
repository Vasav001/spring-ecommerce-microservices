import { createContext, useContext } from 'react'

export type Role = 'CUSTOMER' | 'ADMIN'

export type Session = {
  status: 'loading' | 'anonymous' | 'authenticated'
  name?: string
  roles: string[]
  isAdmin: boolean
  login: (role?: Role) => void
  logout: () => void
  fake: boolean
}

export const SessionContext = createContext<Session | null>(null)

export function useSession() {
  const session = useContext(SessionContext)
  if (!session) {
    throw new Error('useSession must be used inside SessionProvider')
  }
  return session
}

export function rolesFromToken(token?: string): string[] {
  if (!token) {
    return []
  }
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
    return payload.realm_access?.roles ?? []
  } catch {
    return []
  }
}
