import { useState } from 'react'
import { Link, useParams } from 'react-router'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { call, callOrNull } from '../api/call'
import { integrationApi, orderApi } from '../api/client'
import type { OrderDetails, Payment } from '../api/types'
import OrderSummary from '../components/OrderSummary'
import StatusBadge from '../components/StatusBadge'
import { formatDate, formatPrice } from '../lib/format'

const POLL_MS = 2000

export default function OrderPage() {
  const { id = '' } = useParams()
  const order = useQuery({
    queryKey: ['order', id],
    queryFn: () => call(orderApi.GET('/api/v1/orders/{orderId}', { params: { path: { orderId: id } } })),
    refetchInterval: (query) => (query.state.data?.status === 'PENDING' ? POLL_MS : false),
  })

  if (order.isPending) {
    return <p className="text-slate-500">Loading…</p>
  }
  if (order.isError) {
    return <p className="text-red-600">{order.error.message}</p>
  }

  const o = order.data
  const shipped = o.status === 'CONFIRMED' || o.status === 'SHIPPED' || o.status === 'DELIVERED' || o.failureReason === 'SHIPMENT_FAILED'

  return (
    <div className="space-y-8">
      <div className="flex flex-wrap items-center gap-3">
        <h1 className="text-2xl font-semibold">Order</h1>
        <StatusBadge status={o.status} />
        <span className="text-sm text-slate-500">{formatDate(o.createdAt)}</span>
      </div>
      {o.status === 'FAILED' && <p className="text-red-600">This order failed: {o.failureReason}</p>}
      {o.status === 'PENDING' && <PaymentPanel order={o} />}
      {shipped && <ShipmentPanel orderId={id} />}
      <OrderSummary order={o} />
      <Link to="/orders" className="text-sm text-blue-700 hover:underline">← All orders</Link>
    </div>
  )
}

function PaymentPanel({ order }: { order: OrderDetails }) {
  const queryClient = useQueryClient()
  const orderId = order.id ?? ''
  const [customAmount, setCustomAmount] = useState('')

  const payment = useQuery({
    queryKey: ['payment', orderId],
    queryFn: () => callOrNull(integrationApi.GET('/api/v1/payments/{orderId}', { params: { path: { orderId } } })),
    refetchInterval: (query) => (query.state.data?.status === 'PENDING' ? false : POLL_MS),
  })

  const pay = useMutation({
    mutationFn: (body: { amount?: number; fail?: boolean }) =>
      call(integrationApi.POST('/api/v1/payments/{orderId}', { params: { path: { orderId } }, body })),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['payment', orderId] })
      queryClient.invalidateQueries({ queryKey: ['order', orderId] })
    },
  })

  return (
    <section className="space-y-3 rounded-lg border border-amber-200 bg-amber-50 p-4">
      <h2 className="font-semibold">Payment (dummy)</h2>
      <PaymentState payment={payment.data} loading={payment.isPending} />
      {payment.data?.status === 'PENDING' && (
        <div className="flex flex-wrap items-center gap-3">
          <button
            disabled={pay.isPending}
            onClick={() => pay.mutate({ amount: payment.data?.amount })}
            className="rounded bg-slate-900 px-4 py-2 text-white hover:bg-slate-700 disabled:opacity-50"
          >
            Pay {formatPrice(payment.data.amount)}
          </button>
          <input
            value={customAmount}
            onChange={(e) => setCustomAmount(e.target.value)}
            placeholder="Other amount"
            inputMode="decimal"
            className="w-32 rounded border border-slate-300 px-2 py-2 text-sm"
          />
          <button
            disabled={pay.isPending || customAmount === ''}
            onClick={() => pay.mutate({ amount: Number(customAmount) })}
            className="rounded border border-slate-300 bg-white px-3 py-2 text-sm hover:bg-slate-100 disabled:opacity-50"
          >
            Pay this amount
          </button>
          <button
            disabled={pay.isPending}
            onClick={() => pay.mutate({ fail: true })}
            className="rounded border border-red-300 bg-white px-3 py-2 text-sm text-red-700 hover:bg-red-50 disabled:opacity-50"
          >
            Simulate decline
          </button>
        </div>
      )}
      {pay.isError && <p className="text-sm text-red-600">{pay.error.message}</p>}
    </section>
  )
}

function PaymentState({ payment, loading }: { payment: Payment | null | undefined; loading: boolean }) {
  if (loading || payment === null) {
    return <p className="text-sm text-slate-600">Reserving your items… the payment request will appear here shortly.</p>
  }
  if (payment?.status === 'PENDING') {
    return <p className="text-sm text-slate-600">Amount due: {formatPrice(payment.amount)}</p>
  }
  return (
    <p className="text-sm text-slate-600">
      Payment <StatusBadge status={payment?.status} /> {payment?.failureReason && `(${payment.failureReason})`} — updating your order…
    </p>
  )
}

function ShipmentPanel({ orderId }: { orderId: string }) {
  const shipment = useQuery({
    queryKey: ['shipment', orderId],
    queryFn: () => callOrNull(integrationApi.GET('/api/v1/shipments/{orderId}', { params: { path: { orderId } } })),
  })
  return (
    <section className="space-y-2 rounded-lg border border-slate-200 bg-white p-4">
      <h2 className="font-semibold">Shipment</h2>
      {shipment.data
        ? <p className="text-sm">Status: <StatusBadge status={shipment.data.status} /> <span className="text-slate-500">updated {formatDate(shipment.data.updatedAt)}</span></p>
        : <p className="text-sm text-slate-600">Preparing your shipment…</p>}
    </section>
  )
}
