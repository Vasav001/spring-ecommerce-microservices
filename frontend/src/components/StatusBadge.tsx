const colors: Record<string, string> = {
  PENDING: 'bg-amber-100 text-amber-800',
  CREATED: 'bg-amber-100 text-amber-800',
  CONFIRMED: 'bg-blue-100 text-blue-800',
  SUCCEEDED: 'bg-green-100 text-green-800',
  SHIPPED: 'bg-indigo-100 text-indigo-800',
  DISPATCHED: 'bg-indigo-100 text-indigo-800',
  DELIVERED: 'bg-green-100 text-green-800',
  FAILED: 'bg-red-100 text-red-800',
  CANCELLED: 'bg-slate-200 text-slate-700',
  REFUNDED: 'bg-slate-200 text-slate-700',
}

export default function StatusBadge({ status }: { status?: string }) {
  return (
    <span className={`rounded px-2 py-0.5 text-xs font-medium ${colors[status ?? ''] ?? 'bg-slate-100 text-slate-700'}`}>
      {status ?? 'UNKNOWN'}
    </span>
  )
}
