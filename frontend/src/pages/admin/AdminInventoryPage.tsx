import { useState } from 'react'
import { Link } from 'react-router'
import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { call } from '../../api/call'
import { inventoryApi } from '../../api/client'
import { useProducts } from '../../api/queries'
import Pagination from '../../components/Pagination'

export default function AdminInventoryPage() {
  const [page, setPage] = useState(0)
  const inventory = useQuery({
    queryKey: ['admin-inventory', page],
    queryFn: () => call(inventoryApi.GET('/api/v1/admin/inventory', { params: { query: { page, size: 20 } } })),
    placeholderData: keepPreviousData,
  })
  const items = inventory.data?.content ?? []
  const products = useProducts(items.map((item) => item.productId ?? ''))

  return (
    <div className="space-y-4">
      <h2 className="text-lg font-semibold">Inventory</h2>
      {inventory.isPending && <p className="text-slate-500">Loading…</p>}
      {inventory.isError && <p className="text-red-600">{inventory.error.message}</p>}
      {inventory.isSuccess && items.length === 0 && <p className="text-slate-500">No stock records yet.</p>}
      {items.length > 0 && (
        <table className="w-full rounded-lg border border-slate-200 bg-white text-sm">
          <thead className="bg-slate-100 text-left text-slate-600">
            <tr>
              <th className="p-3">Product</th>
              <th className="p-3 text-right">On hand</th>
              <th className="p-3 text-right">Reserved</th>
              <th className="p-3 text-right">Available</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200">
            {items.map((item) => (
              <tr key={item.productId}>
                <td className="p-3">
                  <Link to={`/admin/products/${item.productId}`} className="hover:underline">
                    {products.data?.get(item.productId ?? '')?.name ?? item.productId}
                  </Link>
                </td>
                <td className="p-3 text-right">{item.onHand}</td>
                <td className="p-3 text-right">{item.reserved}</td>
                <td className={`p-3 text-right ${(item.available ?? 0) === 0 ? 'text-red-600' : ''}`}>{item.available}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      <Pagination page={page} totalPages={inventory.data?.page?.totalPages ?? 0} onChange={setPage} />
      <p className="text-xs text-slate-500">To change stock, open a product and use the Stock panel.</p>
    </div>
  )
}
