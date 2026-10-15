import { useState, type FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { call, callOrNull } from '../api/call'
import { inventoryApi } from '../api/client'

export default function StockAdjuster({ productId }: { productId: string }) {
  const queryClient = useQueryClient()
  const [delta, setDelta] = useState('')

  const stock = useQuery({
    queryKey: ['admin-stock', productId],
    queryFn: () => callOrNull(inventoryApi.GET('/api/v1/admin/inventory/{productId}', { params: { path: { productId } } })),
  })
  const adjust = useMutation({
    mutationFn: (value: number) =>
      call(inventoryApi.POST('/api/v1/admin/inventory/{productId}/adjustments', {
        params: { path: { productId } },
        body: { delta: value },
      })),
    onSuccess: (item) => {
      queryClient.setQueryData(['admin-stock', productId], item)
      queryClient.invalidateQueries({ queryKey: ['admin-inventory'] })
      setDelta('')
    },
  })

  const submit = (e: FormEvent) => {
    e.preventDefault()
    const value = Number(delta)
    if (Number.isInteger(value) && value !== 0) {
      adjust.mutate(value)
    }
  }

  const item = stock.data
  return (
    <section className="space-y-3 rounded-lg border border-slate-200 bg-white p-4">
      <h3 className="font-semibold">Stock</h3>
      {stock.isPending && <p className="text-sm text-slate-500">Loading…</p>}
      {stock.isSuccess && (item
        ? <p className="text-sm">On hand <b>{item.onHand}</b> · Reserved <b>{item.reserved}</b> · Available <b>{item.available}</b></p>
        : <p className="text-sm text-slate-500">No stock recorded yet. Add some below.</p>)}
      <form onSubmit={submit} className="flex flex-wrap items-center gap-2">
        <input
          value={delta}
          onChange={(e) => setDelta(e.target.value)}
          type="number"
          step={1}
          placeholder="+10 or -2"
          className="w-32 rounded border border-slate-300 px-2 py-1 text-sm"
        />
        <button
          disabled={adjust.isPending || delta === '' || Number(delta) === 0}
          className="rounded border border-slate-300 px-3 py-1 text-sm hover:bg-slate-100 disabled:opacity-50"
        >
          Adjust
        </button>
      </form>
      {adjust.isError && <p className="text-sm text-red-600">{adjust.error.message}</p>}
    </section>
  )
}
