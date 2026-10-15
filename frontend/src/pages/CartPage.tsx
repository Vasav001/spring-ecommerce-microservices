import { Link } from 'react-router'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { call } from '../api/call'
import { orderApi } from '../api/client'
import { useCart, useProducts } from '../api/queries'
import { formatPrice } from '../lib/format'

export default function CartPage() {
  const queryClient = useQueryClient()
  const cart = useCart()
  const items = cart.data?.items ?? []
  const products = useProducts(items.map((item) => item.productId ?? ''))

  const refresh = () => queryClient.invalidateQueries({ queryKey: ['cart'] })
  const setQuantity = useMutation({
    mutationFn: ({ productId, quantity }: { productId: string; quantity: number }) =>
      call(orderApi.PUT('/api/v1/cart/items/{productId}', { params: { path: { productId } }, body: { quantity } })),
    onSuccess: refresh,
  })
  const remove = useMutation({
    mutationFn: (productId: string) =>
      call(orderApi.DELETE('/api/v1/cart/items/{productId}', { params: { path: { productId } } })),
    onSuccess: refresh,
  })
  const clear = useMutation({
    mutationFn: () => call(orderApi.DELETE('/api/v1/cart')),
    onSuccess: refresh,
  })

  if (cart.isPending) {
    return <p className="text-slate-500">Loading…</p>
  }
  if (cart.isError) {
    return <p className="text-red-600">{cart.error.message}</p>
  }
  if (items.length === 0) {
    return (
      <div className="space-y-3">
        <h1 className="text-2xl font-semibold">Your cart</h1>
        <p className="text-slate-500">Your cart is empty.</p>
        <Link to="/" className="text-blue-700 hover:underline">Continue shopping</Link>
      </div>
    )
  }

  const total = items.reduce((sum, item) => {
    const product = products.data?.get(item.productId ?? '')
    return product?.status === 'ACTIVE' ? sum + (product.price ?? 0) * (item.quantity ?? 0) : sum
  }, 0)
  const hasUnavailable = items.some((item) => products.data?.get(item.productId ?? '')?.status !== 'ACTIVE')
  const error = setQuantity.error ?? remove.error ?? clear.error

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold">Your cart</h1>
      <ul className="divide-y divide-slate-200 rounded-lg border border-slate-200 bg-white">
        {items.map((item) => {
          const productId = item.productId ?? ''
          const product = products.data?.get(productId)
          const unavailable = products.isSuccess && product?.status !== 'ACTIVE'
          const quantity = item.quantity ?? 1
          const options = Array.from(new Set([...Array.from({ length: 10 }, (_, i) => i + 1), quantity])).sort((a, b) => a - b)
          return (
            <li key={productId} className="flex flex-wrap items-center gap-4 p-4">
              <div className="min-w-48 flex-1">
                <Link to={`/products/${productId}`} className="font-medium hover:underline">{product?.name ?? productId}</Link>
                <p className="text-sm text-slate-500">{unavailable ? 'No longer available' : formatPrice(product?.price)}</p>
              </div>
              <select
                value={quantity}
                onChange={(e) => setQuantity.mutate({ productId, quantity: Number(e.target.value) })}
                className="rounded border border-slate-300 px-2 py-1"
              >
                {options.map((n) => <option key={n} value={n}>{n}</option>)}
              </select>
              <p className="w-28 text-right">{unavailable ? '—' : formatPrice((product?.price ?? 0) * quantity)}</p>
              <button onClick={() => remove.mutate(productId)} className="text-sm text-red-600 hover:underline">Remove</button>
            </li>
          )
        })}
      </ul>
      {error && <p className="text-sm text-red-600">{error.message}</p>}
      <div className="flex flex-wrap items-center gap-4">
        <p className="text-lg">Total: <span className="font-semibold">{formatPrice(total)}</span></p>
        <button onClick={() => clear.mutate()} className="rounded border border-slate-300 px-3 py-2 text-sm hover:bg-slate-100">Clear cart</button>
        {hasUnavailable
          ? <p className="text-sm text-red-600">Remove unavailable items to check out.</p>
          : <Link to="/checkout" className="rounded bg-slate-900 px-4 py-2 text-white hover:bg-slate-700">Checkout</Link>}
      </div>
    </div>
  )
}
