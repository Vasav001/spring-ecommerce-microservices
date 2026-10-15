import { Link } from 'react-router'
import type { OrderDetails } from '../api/types'
import { formatPrice } from '../lib/format'

export default function OrderSummary({ order }: { order: OrderDetails }) {
  const address = order.shippingAddress
  return (
    <section className="grid gap-6 md:grid-cols-2">
      <div className="space-y-2">
        <h2 className="font-semibold">Items</h2>
        <ul className="space-y-1 text-sm">
          {order.items?.map((item) => (
            <li key={item.productId} className="flex justify-between gap-4">
              <Link to={`/products/${item.productId}`} className="hover:underline">{item.productName} × {item.quantity}</Link>
              <span>{formatPrice((item.unitPrice ?? 0) * (item.quantity ?? 0))}</span>
            </li>
          ))}
        </ul>
        <p className="font-medium">Total: {formatPrice(order.totalAmount)}</p>
      </div>
      {address && (
        <div className="space-y-1 text-sm">
          <h2 className="font-semibold">Shipping to</h2>
          <p>{address.name} · {address.phone}</p>
          <p>{address.line1}{address.line2 ? `, ${address.line2}` : ''}</p>
          <p>{address.city}, {address.state} {address.postalCode}, {address.country}</p>
        </div>
      )}
    </section>
  )
}
