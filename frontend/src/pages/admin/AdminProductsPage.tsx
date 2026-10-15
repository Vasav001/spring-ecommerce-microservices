import { useState } from 'react'
import { Link } from 'react-router'
import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { call } from '../../api/call'
import { catalogApi } from '../../api/client'
import Pagination from '../../components/Pagination'
import StatusBadge from '../../components/StatusBadge'
import { formatDate, formatPrice } from '../../lib/format'

export default function AdminProductsPage() {
  const [page, setPage] = useState(0)
  const products = useQuery({
    queryKey: ['admin-products', page],
    queryFn: () => call(catalogApi.GET('/api/v1/admin/products', { params: { query: { page, size: 20, sort: ['name,asc'] } } })),
    placeholderData: keepPreviousData,
  })

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold">Products</h2>
        <Link to="/admin/products/new" className="rounded bg-slate-900 px-4 py-2 text-sm text-white hover:bg-slate-700">New product</Link>
      </div>
      {products.isPending && <p className="text-slate-500">Loading…</p>}
      {products.isError && <p className="text-red-600">{products.error.message}</p>}
      {products.data?.content?.length === 0 && <p className="text-slate-500">No products yet.</p>}
      <ul className="divide-y divide-slate-200 rounded-lg border border-slate-200 bg-white">
        {products.data?.content?.map((product) => (
          <li key={product.id}>
            <Link to={`/admin/products/${product.id}`} className="flex flex-wrap items-center gap-4 p-3 hover:bg-slate-50">
              {product.imageUrl
                ? <img src={product.imageUrl} alt="" className="h-10 w-10 rounded object-cover" />
                : <div className="h-10 w-10 rounded bg-slate-200" />}
              <span className="flex-1 font-medium">{product.name}</span>
              <span className="w-28 text-right">{formatPrice(product.price)}</span>
              <StatusBadge status={product.status} />
              <span className="w-44 text-right text-xs text-slate-500">{formatDate(product.updatedAt)}</span>
            </Link>
          </li>
        ))}
      </ul>
      <Pagination page={page} totalPages={products.data?.page?.totalPages ?? 0} onChange={setPage} />
    </div>
  )
}
