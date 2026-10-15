import { useState } from 'react'
import { Link } from 'react-router'
import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { call } from '../api/call'
import { orderApi } from '../api/client'
import Pagination from '../components/Pagination'
import StatusBadge from '../components/StatusBadge'
import { formatDate, formatPrice } from '../lib/format'

export default function OrdersPage() {
  const [page, setPage] = useState(0)
  const orders = useQuery({
    queryKey: ['orders', page],
    queryFn: () => call(orderApi.GET('/api/v1/orders', { params: { query: { page, size: 10 } } })),
    placeholderData: keepPreviousData,
  })

  if (orders.isPending) {
    return <p className="text-slate-500">Loading…</p>
  }
  if (orders.isError) {
    return <p className="text-red-600">{orders.error.message}</p>
  }

  const content = orders.data.content ?? []
  const totalPages = orders.data.page?.totalPages ?? 0

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold">My orders</h1>
      {content.length === 0 && <p className="text-slate-500">You have no orders yet.</p>}
      <ul className="divide-y divide-slate-200 rounded-lg border border-slate-200 bg-white">
        {content.map((order) => (
          <li key={order.id}>
            <Link to={`/orders/${order.id}`} className="flex flex-wrap items-center gap-4 p-4 hover:bg-slate-50">
              <span className="flex-1 text-sm text-slate-500">{formatDate(order.createdAt)}</span>
              <span className="text-sm">{order.items?.length ?? 0} item(s)</span>
              <span className="w-28 text-right font-medium">{formatPrice(order.totalAmount)}</span>
              <StatusBadge status={order.status} />
            </Link>
          </li>
        ))}
      </ul>
      <Pagination page={page} totalPages={totalPages} onChange={setPage} />
    </div>
  )
}
