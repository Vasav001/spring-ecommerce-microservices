import { Link, useParams } from 'react-router'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { call, callOrNull } from '../../api/call'
import { integrationApi, orderApi } from '../../api/client'
import type { OrderDetails } from '../../api/types'
import OrderSummary from '../../components/OrderSummary'
import StatusBadge from '../../components/StatusBadge'
import { formatDate } from '../../lib/format'

const POLL_MS = 3000
const SETTLED = ['FAILED', 'DELIVERED']

type Action = 'dispatch' | 'deliver' | 'fail'

export default function AdminOrderPage() {
  const { id = '' } = useParams()
  const order = useQuery({
    queryKey: ['admin-order', id],
    queryFn: () => call(orderApi.GET('/api/v1/admin/orders/{orderId}', { params: { path: { orderId: id } } })),
    refetchInterval: (query) => (SETTLED.includes(query.state.data?.status ?? '') ? false : POLL_MS),
  })

  if (order.isPending) {
    return <p className="text-slate-500">Loading…</p>
  }
  if (order.isError) {
    return <p className="text-red-600">{order.error.message}</p>
  }

  const o = order.data
  return (
    <div className="space-y-6">
      <Link to="/admin/orders" className="text-sm text-blue-700 hover:underline">← All orders</Link>
      <div className="flex flex-wrap items-center gap-3">
        <h2 className="text-lg font-semibold">Order {o.id}</h2>
        <StatusBadge status={o.status} />
        <span className="text-sm text-slate-500">{formatDate(o.createdAt)}</span>
      </div>
      {o.failureReason && <p className="text-red-600">Failure reason: {o.failureReason}</p>}
      <ShipmentControls order={o} />
      <OrderSummary order={o} />
    </div>
  )
}

function ShipmentControls({ order }: { order: OrderDetails }) {
  const queryClient = useQueryClient()
  const orderId = order.id ?? ''
  const expectShipment = order.status !== 'PENDING' && !(order.status === 'FAILED' && order.failureReason !== 'SHIPMENT_FAILED')

  const shipment = useQuery({
    queryKey: ['admin-shipment', orderId],
    enabled: expectShipment,
    queryFn: () => callOrNull(integrationApi.GET('/api/v1/admin/shipments/{orderId}', { params: { path: { orderId } } })),
    refetchInterval: (query) => (query.state.data ? false : POLL_MS),
  })

  const act = useMutation({
    mutationFn: (action: Action) => {
      const options = { params: { path: { orderId } } }
      if (action === 'dispatch') {
        return call(integrationApi.POST('/api/v1/admin/shipments/{orderId}/dispatch', options))
      }
      if (action === 'deliver') {
        return call(integrationApi.POST('/api/v1/admin/shipments/{orderId}/deliver', options))
      }
      return call(integrationApi.POST('/api/v1/admin/shipments/{orderId}/fail', options))
    },
    onSuccess: (updated) => {
      queryClient.setQueryData(['admin-shipment', orderId], updated)
      queryClient.invalidateQueries({ queryKey: ['admin-order', orderId] })
      queryClient.invalidateQueries({ queryKey: ['admin-orders'] })
    },
  })

  if (!expectShipment) {
    return null
  }

  const s = shipment.data
  const button = 'rounded border px-3 py-1 text-sm disabled:opacity-50'
  return (
    <section className="space-y-3 rounded-lg border border-slate-200 bg-white p-4">
      <h3 className="font-semibold">Shipment (dummy)</h3>
      {!s && <p className="text-sm text-slate-500">Waiting for the shipment to be created…</p>}
      {s && (
        <>
          <p className="text-sm">Status: <StatusBadge status={s.status} /> <span className="text-slate-500">updated {formatDate(s.updatedAt)}</span></p>
          <div className="flex flex-wrap gap-2">
            {s.status === 'CREATED' && (
              <button disabled={act.isPending} onClick={() => act.mutate('dispatch')} className={`${button} border-slate-300 hover:bg-slate-100`}>
                Dispatch
              </button>
            )}
            {s.status === 'DISPATCHED' && (
              <button disabled={act.isPending} onClick={() => act.mutate('deliver')} className={`${button} border-slate-300 hover:bg-slate-100`}>
                Mark delivered
              </button>
            )}
            {(s.status === 'CREATED' || s.status === 'DISPATCHED') && (
              <button disabled={act.isPending} onClick={() => act.mutate('fail')} className={`${button} border-red-300 text-red-700 hover:bg-red-50`}>
                Fail shipment
              </button>
            )}
          </div>
          {s.status === 'FAILED' && <p className="text-sm text-slate-500">The order was refunded and the stock returned.</p>}
        </>
      )}
      {act.isError && <p className="text-sm text-red-600">{act.error.message}</p>}
    </section>
  )
}
