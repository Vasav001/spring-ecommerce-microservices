import { useState } from 'react'
import { Link } from 'react-router'
import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { searchApi } from '../api/client'
import Pagination from '../components/Pagination'
import { formatPrice } from '../lib/format'

const PAGE_SIZE = 12

export default function SearchPage() {
  const [q, setQ] = useState('')
  const [inStock, setInStock] = useState(false)
  const [sort, setSort] = useState('name,asc')
  const [page, setPage] = useState(0)

  const query = useQuery({
    queryKey: ['search', q, inStock, sort, page],
    queryFn: async () => {
      const { data, error } = await searchApi.GET('/api/v1/search', {
        params: { query: { q: q || undefined, inStock: inStock || undefined, sort: [sort], page, size: PAGE_SIZE } },
      })
      if (error) {
        throw new Error('Search failed')
      }
      return data
    },
    placeholderData: keepPreviousData,
  })

  const results = query.data?.content ?? []
  const totalPages = query.data?.page?.totalPages ?? 0

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center gap-3">
        <input
          value={q}
          onChange={(e) => { setQ(e.target.value); setPage(0) }}
          placeholder="Search products"
          className="w-full max-w-md rounded border border-slate-300 px-3 py-2"
        />
        <label className="flex items-center gap-2 text-sm">
          <input type="checkbox" checked={inStock} onChange={(e) => { setInStock(e.target.checked); setPage(0) }} />
          In stock only
        </label>
        <select
          value={sort}
          onChange={(e) => { setSort(e.target.value); setPage(0) }}
          className="rounded border border-slate-300 px-2 py-2 text-sm"
        >
          <option value="name,asc">Name</option>
          <option value="price,asc">Price: low to high</option>
          <option value="price,desc">Price: high to low</option>
          <option value="averageRating,desc">Top rated</option>
        </select>
      </div>

      {query.isError && <p className="text-red-600">Could not load products.</p>}
      {query.isPending && <p className="text-slate-500">Loading…</p>}
      {query.isSuccess && results.length === 0 && <p className="text-slate-500">No products found.</p>}

      <ul className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {results.map((product) => (
          <li key={product.productId}>
            <Link to={`/products/${product.productId}`} className="block rounded-lg border border-slate-200 bg-white p-4 hover:shadow">
              {product.imageUrl && <img src={product.imageUrl} alt="" className="mb-3 h-40 w-full rounded object-cover" />}
              <h2 className="font-semibold">{product.name}</h2>
              <p className="text-slate-700">{formatPrice(product.price)}</p>
              <p className="text-sm text-slate-500">
                {product.inStock ? 'In stock' : 'Out of stock'} · ★ {product.averageRating} ({product.reviewCount})
              </p>
            </Link>
          </li>
        ))}
      </ul>

      <Pagination page={page} totalPages={totalPages} onChange={setPage} />
    </div>
  )
}
