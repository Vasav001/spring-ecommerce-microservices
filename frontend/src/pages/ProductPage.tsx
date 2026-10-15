import { useState } from 'react'
import { Link, useParams } from 'react-router'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { call } from '../api/call'
import { catalogApi, inventoryApi, orderApi } from '../api/client'
import { useCart } from '../api/queries'
import { useSession } from '../auth/session'
import MyReview from '../components/MyReview'
import { formatDate, formatPrice } from '../lib/format'

const MAX_QUANTITY = 99

export default function ProductPage() {
  const { id = '' } = useParams()
  const session = useSession()
  const loggedIn = session.status === 'authenticated'
  const queryClient = useQueryClient()
  const [quantity, setQuantity] = useState(1)

  const product = useQuery({
    queryKey: ['product', id],
    queryFn: () => call(catalogApi.GET('/api/v1/products/{id}', { params: { path: { id } } })),
  })
  const stock = useQuery({
    queryKey: ['stock', id],
    enabled: loggedIn,
    queryFn: () => call(inventoryApi.GET('/api/v1/inventory', { params: { query: { productIds: [id] } } })),
  })
  const reviews = useQuery({
    queryKey: ['reviews', id],
    queryFn: () => call(catalogApi.GET('/api/v1/reviews', { params: { query: { productId: id, page: 0, size: 20 } } })),
  })
  const cart = useCart(loggedIn)
  const inCart = cart.data?.items?.find((item) => item.productId === id)?.quantity ?? 0

  const addToCart = useMutation({
    mutationFn: () =>
      call(orderApi.PUT('/api/v1/cart/items/{productId}', {
        params: { path: { productId: id } },
        body: { quantity: Math.min(MAX_QUANTITY, inCart + quantity) },
      })),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['cart'] }),
  })

  if (product.isPending) {
    return <p className="text-slate-500">Loading…</p>
  }
  if (product.isError) {
    return <p className="text-red-600">{product.error.message}</p>
  }

  const p = product.data
  const available = stock.data?.[0]

  return (
    <div className="space-y-8">
      <div className="grid gap-6 md:grid-cols-2">
        {p.imageUrl
          ? <img src={p.imageUrl} alt="" className="w-full rounded-lg object-cover" />
          : <div className="flex aspect-square items-center justify-center rounded-lg bg-slate-200 text-slate-500">No image</div>}
        <div className="space-y-4">
          <h1 className="text-2xl font-semibold">{p.name}</h1>
          <p className="text-xl">{formatPrice(p.price)}</p>
          {p.description && <p className="text-slate-700">{p.description}</p>}
          {available && (
            <p className={available.inStock ? 'text-green-700' : 'text-red-600'}>
              {available.inStock ? `${available.available} in stock` : 'Out of stock'}
            </p>
          )}
          {loggedIn ? (
            <div className="flex flex-wrap items-center gap-3">
              <select
                value={quantity}
                onChange={(e) => setQuantity(Number(e.target.value))}
                className="rounded border border-slate-300 px-2 py-2"
              >
                {Array.from({ length: 10 }, (_, i) => i + 1).map((n) => <option key={n} value={n}>{n}</option>)}
              </select>
              <button
                onClick={() => addToCart.mutate()}
                disabled={addToCart.isPending || available?.inStock === false}
                className="rounded bg-slate-900 px-4 py-2 text-white hover:bg-slate-700 disabled:opacity-50"
              >
                Add to cart
              </button>
              {inCart > 0 && <Link to="/cart" className="text-sm text-blue-700 hover:underline">{inCart} in cart · View cart</Link>}
            </div>
          ) : (
            <button onClick={() => session.login()} className="rounded bg-slate-900 px-4 py-2 text-white hover:bg-slate-700">
              Log in to buy
            </button>
          )}
          {addToCart.isError && <p className="text-sm text-red-600">{addToCart.error.message}</p>}
        </div>
      </div>

      <section className="space-y-3">
        <h2 className="text-lg font-semibold">Reviews</h2>
        {loggedIn && <MyReview productId={id} />}
        {reviews.isPending && <p className="text-slate-500">Loading…</p>}
        {reviews.data?.content?.length === 0 && <p className="text-slate-500">No reviews yet.</p>}
        <ul className="space-y-3">
          {reviews.data?.content?.map((review) => (
            <li key={review.id} className="rounded border border-slate-200 bg-white p-3">
              <p className="text-sm">
                <span className="font-medium">{review.authorName}</span>
                <span className="ml-2 text-amber-600">{'★'.repeat(Math.min(5, Math.max(0, review.rating ?? 0)))}</span>
                <span className="ml-2 text-slate-400">{formatDate(review.createdAt)}</span>
              </p>
              {review.comment && <p className="mt-1 text-slate-700">{review.comment}</p>}
            </li>
          ))}
        </ul>
      </section>
    </div>
  )
}
