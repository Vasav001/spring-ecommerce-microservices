import type { ReactNode } from 'react'
import { useSession } from '../auth/session'

export default function RequireAuth({ admin = false, children }: { admin?: boolean; children: ReactNode }) {
  const session = useSession()
  if (session.status === 'loading') {
    return <p className="text-slate-500">Loading…</p>
  }
  if (session.status === 'anonymous') {
    return (
      <div className="space-y-3">
        <p>Please log in to continue.</p>
        <button onClick={() => session.login()} className="rounded bg-slate-900 px-3 py-1 text-sm text-white hover:bg-slate-700">
          Log in
        </button>
      </div>
    )
  }
  if (admin && !session.isAdmin) {
    return <p>This page is for admins only.</p>
  }
  return children
}
