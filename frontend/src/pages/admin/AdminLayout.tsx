import { NavLink, Outlet } from 'react-router'

const tabClass = ({ isActive }: { isActive: boolean }) =>
  `rounded px-3 py-1 text-sm ${isActive ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-200'}`

export default function AdminLayout() {
  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center gap-2">
        <h1 className="mr-4 text-2xl font-semibold">Admin</h1>
        <NavLink to="/admin/products" className={tabClass}>Products</NavLink>
        <NavLink to="/admin/inventory" className={tabClass}>Inventory</NavLink>
        <NavLink to="/admin/orders" className={tabClass}>Orders</NavLink>
      </div>
      <Outlet />
    </div>
  )
}
