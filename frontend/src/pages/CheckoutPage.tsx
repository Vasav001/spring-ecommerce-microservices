import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { call } from '../api/call'
import { customerApi, orderApi } from '../api/client'
import { useCart, useProducts, useProfile } from '../api/queries'
import type { Address, AddressInput, ShippingAddress } from '../api/types'
import AddressForm from '../components/AddressForm'
import { formatPrice } from '../lib/format'

function toShippingAddress(address: Address): ShippingAddress {
  return {
    name: address.name ?? '',
    phone: address.phone ?? '',
    line1: address.line1 ?? '',
    line2: address.line2,
    city: address.city ?? '',
    state: address.state ?? '',
    postalCode: address.postalCode ?? '',
    country: address.country ?? '',
  }
}

export default function CheckoutPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [idempotencyKey] = useState(() => crypto.randomUUID())
  const [selectedId, setSelectedId] = useState<string>()
  const [adding, setAdding] = useState(false)

  const profile = useProfile()
  const cart = useCart()
  const items = cart.data?.items ?? []
  const products = useProducts(items.map((item) => item.productId ?? ''))
  const addresses = profile.data?.addresses ?? []
  const selected = addresses.find((a) => a.id === selectedId) ?? (addresses.length === 1 ? addresses[0] : undefined)

  const addAddress = useMutation({
    mutationFn: (address: AddressInput) => call(customerApi.POST('/api/v1/customers/me/addresses', { body: address })),
    onSuccess: async (created) => {
      await queryClient.invalidateQueries({ queryKey: ['profile'] })
      setSelectedId(created.id)
      setAdding(false)
    },
  })

  const placeOrder = useMutation({
    mutationFn: (address: Address) =>
      call(orderApi.POST('/api/v1/checkout', {
        params: { header: { 'Idempotency-Key': idempotencyKey } },
        body: { shippingAddress: toShippingAddress(address) },
      })),
    onSuccess: (order) => {
      queryClient.invalidateQueries({ queryKey: ['cart'] })
      navigate(`/orders/${order.id}`)
    },
  })

  if (profile.isPending || cart.isPending) {
    return <p className="text-slate-500">Loading…</p>
  }
  if (profile.isError || cart.isError) {
    return <p className="text-red-600">{(profile.error ?? cart.error)?.message}</p>
  }
  if (items.length === 0) {
    return (
      <div className="space-y-3">
        <p className="text-slate-500">Your cart is empty.</p>
        <Link to="/" className="text-blue-700 hover:underline">Continue shopping</Link>
      </div>
    )
  }

  const total = items.reduce((sum, item) => sum + (products.data?.get(item.productId ?? '')?.price ?? 0) * (item.quantity ?? 0), 0)

  return (
    <div className="space-y-8">
      <h1 className="text-2xl font-semibold">Checkout</h1>

      <section className="space-y-3">
        <h2 className="text-lg font-semibold">Ship to</h2>
        {addresses.length === 0 && !adding && <p className="text-slate-500">You have no saved addresses yet.</p>}
        <ul className="space-y-2">
          {addresses.map((address) => (
            <li key={address.id}>
              <label className="flex cursor-pointer gap-3 rounded border border-slate-200 bg-white p-3">
                <input
                  type="radio"
                  name="address"
                  checked={selected?.id === address.id}
                  onChange={() => setSelectedId(address.id)}
                />
                <span className="text-sm">
                  <span className="font-medium">{address.name}</span> · {address.phone}<br />
                  {address.line1}{address.line2 ? `, ${address.line2}` : ''}, {address.city}, {address.state} {address.postalCode}, {address.country}
                </span>
              </label>
            </li>
          ))}
        </ul>
        {adding ? (
          <AddressForm
            submitLabel="Save address"
            submitting={addAddress.isPending}
            error={addAddress.error?.message}
            onSubmit={(address) => addAddress.mutate(address)}
            onCancel={addresses.length > 0 ? () => setAdding(false) : undefined}
          />
        ) : (
          <button onClick={() => setAdding(true)} className="text-sm text-blue-700 hover:underline">+ Add a new address</button>
        )}
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-semibold">Order summary</h2>
        <ul className="space-y-1 text-sm">
          {items.map((item) => {
            const product = products.data?.get(item.productId ?? '')
            return (
              <li key={item.productId} className="flex justify-between">
                <span>{product?.name ?? item.productId} × {item.quantity}</span>
                <span>{formatPrice((product?.price ?? 0) * (item.quantity ?? 0))}</span>
              </li>
            )
          })}
        </ul>
        <p className="text-lg">Total: <span className="font-semibold">{formatPrice(total)}</span></p>
        <p className="text-xs text-slate-500">The final price is confirmed when the order is placed.</p>
      </section>

      {placeOrder.isError && <p className="text-red-600">{placeOrder.error.message}</p>}
      <button
        disabled={!selected || placeOrder.isPending}
        onClick={() => selected && placeOrder.mutate(selected)}
        className="rounded bg-slate-900 px-6 py-3 text-white hover:bg-slate-700 disabled:opacity-50"
      >
        {placeOrder.isPending ? 'Placing order…' : 'Place order'}
      </button>
    </div>
  )
}
