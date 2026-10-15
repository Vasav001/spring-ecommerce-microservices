import { useState, type ChangeEvent, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { call } from '../../api/call'
import { catalogApi, integrationApi } from '../../api/client'
import type { Product } from '../../api/types'
import StockAdjuster from '../../components/StockAdjuster'
import { uploadImage } from '../../lib/cloudinary'

type Status = 'ACTIVE' | 'INACTIVE'

export default function AdminProductEditPage() {
  const { id } = useParams()
  const product = useQuery({
    queryKey: ['admin-product', id],
    enabled: id !== undefined,
    queryFn: () => call(catalogApi.GET('/api/v1/admin/products/{id}', { params: { path: { id: id ?? '' } } })),
  })

  if (id && product.isPending) {
    return <p className="text-slate-500">Loading…</p>
  }
  if (product.isError) {
    return <p className="text-red-600">{product.error.message}</p>
  }

  return (
    <div className="space-y-6">
      <Link to="/admin/products" className="text-sm text-blue-700 hover:underline">← All products</Link>
      <div className="grid gap-6 lg:grid-cols-[2fr_1fr]">
        <ProductForm key={product.data?.id ?? 'new'} existing={product.data} />
        {id && <StockAdjuster productId={id} />}
      </div>
    </div>
  )
}

function ProductForm({ existing }: { existing?: Product }) {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [name, setName] = useState(existing?.name ?? '')
  const [description, setDescription] = useState(existing?.description ?? '')
  const [price, setPrice] = useState(existing?.price?.toString() ?? '')
  const [status, setStatus] = useState<Status>(existing?.status ?? 'ACTIVE')
  const [imageUrl, setImageUrl] = useState(existing?.imageUrl)

  const upload = useMutation({
    mutationFn: (file: File) => uploadImage(file, () => call(integrationApi.POST('/api/v1/admin/uploads/signature'))),
    onSuccess: setImageUrl,
  })

  const save = useMutation({
    mutationFn: () => {
      const body = { name, description: description || undefined, price: Number(price), imageUrl, status }
      return existing?.id
        ? call(catalogApi.PUT('/api/v1/admin/products/{id}', { params: { path: { id: existing.id } }, body }))
        : call(catalogApi.POST('/api/v1/admin/products', { body }))
    },
    onSuccess: (saved) => {
      queryClient.invalidateQueries({ queryKey: ['admin-products'] })
      queryClient.setQueryData(['admin-product', saved.id], saved)
      if (!existing) {
        navigate(`/admin/products/${saved.id}`, { replace: true })
      }
    },
  })

  const onFile = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      upload.mutate(file)
    }
    e.target.value = ''
  }

  const submit = (e: FormEvent) => {
    e.preventDefault()
    save.mutate()
  }

  return (
    <form onSubmit={submit} className="space-y-4 rounded-lg border border-slate-200 bg-white p-4">
      <h2 className="text-lg font-semibold">{existing ? 'Edit product' : 'New product'}</h2>
      <label className="block text-sm">
        <span className="text-slate-600">Name</span>
        <input value={name} onChange={(e) => setName(e.target.value)} required maxLength={200} className="mt-1 w-full rounded border border-slate-300 px-3 py-2" />
      </label>
      <label className="block text-sm">
        <span className="text-slate-600">Description</span>
        <textarea value={description} onChange={(e) => setDescription(e.target.value)} maxLength={4000} rows={4} className="mt-1 w-full rounded border border-slate-300 px-3 py-2" />
      </label>
      <div className="flex flex-wrap gap-4">
        <label className="text-sm">
          <span className="text-slate-600">Price (₹)</span>
          <input
            value={price}
            onChange={(e) => setPrice(e.target.value)}
            required
            type="number"
            min={0}
            step="0.01"
            className="mt-1 block w-40 rounded border border-slate-300 px-3 py-2"
          />
        </label>
        <label className="text-sm">
          <span className="text-slate-600">Status</span>
          <select value={status} onChange={(e) => setStatus(e.target.value as Status)} className="mt-1 block rounded border border-slate-300 px-3 py-2">
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
          </select>
        </label>
      </div>
      <div className="space-y-2 text-sm">
        <span className="text-slate-600">Image</span>
        <div className="flex flex-wrap items-center gap-4">
          {imageUrl
            ? <img src={imageUrl} alt="" className="h-28 w-28 rounded object-cover" />
            : <div className="flex h-28 w-28 items-center justify-center rounded bg-slate-200 text-slate-500">No image</div>}
          <label className={`cursor-pointer rounded border border-slate-300 px-3 py-1 hover:bg-slate-100 ${upload.isPending ? 'pointer-events-none opacity-50' : ''}`}>
            {upload.isPending ? 'Uploading…' : imageUrl ? 'Replace image' : 'Upload image'}
            <input type="file" accept="image/*" onChange={onFile} className="hidden" />
          </label>
          {imageUrl && (
            <button type="button" onClick={() => setImageUrl(undefined)} className="rounded border border-slate-300 px-3 py-1 hover:bg-slate-100">
              Remove
            </button>
          )}
        </div>
        {upload.isError && <p className="text-red-600">{upload.error.message}</p>}
      </div>
      {save.isError && <p className="text-sm text-red-600">{save.error.message}</p>}
      {save.isSuccess && existing && <p className="text-sm text-green-700">Saved.</p>}
      <button disabled={save.isPending || upload.isPending} className="rounded bg-slate-900 px-4 py-2 text-sm text-white hover:bg-slate-700 disabled:opacity-50">
        {save.isPending ? 'Saving…' : existing ? 'Save changes' : 'Create product'}
      </button>
    </form>
  )
}
