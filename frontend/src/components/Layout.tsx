import { NavLink, Outlet } from 'react-router'
import { useSession } from '../auth/session'

const linkClass = ({ isActive }: { isActive: boolean }) =>
  isActive ? 'font-semibold text-slate-900' : 'text-slate-600 hover:text-slate-900'

const buttonClass = 'rounded border border-slate-300 px-3 py-1 hover:bg-slate-100'

export default function Layout() {
  const session = useSession()
  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center gap-4 px-4 py-3">
          <NavLink to="/" className="text-lg font-bold">Ecom</NavLink>
          <nav className="flex flex-wrap gap-4 text-sm">
            <NavLink to="/" end className={linkClass}>Shop</NavLink>
            <NavLink to="/cart" className={linkClass}>Cart</NavLink>
            <NavLink to="/orders" className={linkClass}>Orders</NavLink>
            <NavLink to="/profile" className={linkClass}>Profile</NavLink>
            {session.isAdmin && <NavLink to="/admin" className={linkClass}>Admin</NavLink>}
          </nav>
          <div className="ml-auto flex items-center gap-2 text-sm">
            <SessionControls />
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-6">
        <Outlet />
      </main>
    </div>
  )
}

function SessionControls() {
  const session = useSession()
  if (session.status === 'loading') {
    return <span className="text-slate-500">Loading…</span>
  }
  if (session.status === 'authenticated') {
    return (
      <>
        <span className="text-slate-600">{session.name}</span>
        <button onClick={session.logout} className={buttonClass}>Log out</button>
      </>
    )
  }
  if (session.fake) {
    return (
      <>
        <button onClick={() => session.login('CUSTOMER')} className={buttonClass}>Mock customer</button>
        <button onClick={() => session.login('ADMIN')} className={buttonClass}>Mock admin</button>
      </>
    )
  }
  return (
    <button onClick={() => session.login()} className="rounded bg-slate-900 px-3 py-1 text-white hover:bg-slate-700">
      Log in
    </button>
  )
}
