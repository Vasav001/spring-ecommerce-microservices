import { useState } from 'react'
import { Link } from 'react-router'
import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { call } from '../../api/call'
import { orderApi } from '../../api/client'
import Pagination from '../../components/Pagination'
import StatusBadge from '../../components/StatusBadge'
import { formatDate, formatPrice } from '../../lib/format'

const statuses = ['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'FAILED'] as const
type Status = (typeof statuses)[number]

export default function AdminOrdersPage() {
  const [status, setStatus] = useState<Status | ''>('')
  const [page, setPage] = useState(0)
  const orders = useQuery({
    queryKey: ['admin-orders', status, page],
    queryFn: () =>
      call(orderApi.GET('/api/v1/admin/orders', { params: { query: { status: status || undefined, page, size: 20 } } })),
    placeholderData: keepPreviousData,
  })

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center gap-3">
        <h2 className="text-lg font-semibold">Orders</h2>
        <select
          value={status}
          onChange={(e) => { setStatus(e.target.value as Status | ''); setPage(0) }}
          className="rounded border border-slate-300 px-2 py-1 text-sm"
        >
          <option value="">All statuses</option>
          {statuses.map((s) => <option key={s} value={s}>{s}</option>)}
        </select>
      </div>
      {orders.isPending && <p className="text-slate-500">Loading…</p>}
      {orders.isError && <p className="text-red-600">{orders.error.message}</p>}
      {orders.data?.content?.length === 0 && <p className="text-slate-500">No orders.</p>}
      <ul className="divide-y divide-slate-200 rounded-lg border border-slate-200 bg-white">
        {orders.data?.content?.map((order) => (
          <li key={order.id}>
            <Link to={`/admin/orders/${order.id}`} className="flex flex-wrap items-center gap-4 p-3 text-sm hover:bg-slate-50">
              <span className="w-44 text-slate-500">{formatDate(order.createdAt)}</span>
              <span className="flex-1 truncate">{order.shippingAddress?.name} · {order.items?.length ?? 0} item(s)</span>
              <span className="w-28 text-right font-medium">{formatPrice(order.totalAmount)}</span>
              <StatusBadge status={order.status} />
            </Link>
          </li>
        ))}
      </ul>
      <Pagination page={page} totalPages={orders.data?.page?.totalPages ?? 0} onChange={setPage} />
    </div>
  )
}
