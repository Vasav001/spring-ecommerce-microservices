import { useState, type FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { call, callOrNull } from '../api/call'
import { catalogApi } from '../api/client'
import type { Review } from '../api/types'

export default function MyReview({ productId }: { productId: string }) {
  const mine = useQuery({
    queryKey: ['my-review', productId],
    queryFn: () => callOrNull(catalogApi.GET('/api/v1/reviews/mine', { params: { query: { productId } } })),
  })

  if (mine.isPending) {
    return null
  }
  return <ReviewForm key={mine.data?.id ?? 'new'} productId={productId} existing={mine.data ?? undefined} />
}

function ReviewForm({ productId, existing }: { productId: string; existing?: Review }) {
  const queryClient = useQueryClient()
  const [rating, setRating] = useState(existing?.rating ?? 5)
  const [comment, setComment] = useState(existing?.comment ?? '')

  const refresh = () => {
    queryClient.invalidateQueries({ queryKey: ['my-review', productId] })
    queryClient.invalidateQueries({ queryKey: ['reviews', productId] })
  }
  const save = useMutation({
    mutationFn: () =>
      call(catalogApi.PUT('/api/v1/products/{productId}/review', {
        params: { path: { productId } },
        body: { rating, comment: comment || undefined },
      })),
    onSuccess: refresh,
  })
  const remove = useMutation({
    mutationFn: () => call(catalogApi.DELETE('/api/v1/products/{productId}/review', { params: { path: { productId } } })),
    onSuccess: refresh,
  })

  const submit = (e: FormEvent) => {
    e.preventDefault()
    save.mutate()
  }
  const error = save.error ?? remove.error

  return (
    <form onSubmit={submit} className="space-y-3 rounded border border-slate-200 bg-white p-4">
      <h3 className="font-medium">{existing ? 'Your review' : 'Write a review'}</h3>
      <div className="flex gap-1">
        {[1, 2, 3, 4, 5].map((n) => (
          <button
            type="button"
            key={n}
            onClick={() => setRating(n)}
            aria-label={`${n} star${n > 1 ? 's' : ''}`}
            className={`text-2xl ${n <= rating ? 'text-amber-500' : 'text-slate-300'}`}
          >
            ★
          </button>
        ))}
      </div>
      <textarea
        value={comment}
        onChange={(e) => setComment(e.target.value)}
        maxLength={2000}
        rows={3}
        placeholder="What did you think? (optional)"
        className="w-full rounded border border-slate-300 px-3 py-2 text-sm"
      />
      {error && <p className="text-sm text-red-600">{error.message}</p>}
      <div className="flex gap-2">
        <button disabled={save.isPending} className="rounded bg-slate-900 px-4 py-2 text-sm text-white hover:bg-slate-700 disabled:opacity-50">
          {existing ? 'Update review' : 'Post review'}
        </button>
        {existing && (
          <button
            type="button"
            disabled={remove.isPending}
            onClick={() => remove.mutate()}
            className="rounded border border-slate-300 px-4 py-2 text-sm hover:bg-slate-100 disabled:opacity-50"
          >
            Delete
          </button>
        )}
      </div>
      {save.isSuccess && <p className="text-sm text-green-700">Thanks, your review is saved.</p>}
    </form>
  )
}
