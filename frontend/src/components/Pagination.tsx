export default function Pagination({ page, totalPages, onChange }: { page: number; totalPages: number; onChange: (page: number) => void }) {
  if (totalPages <= 1) {
    return null
  }
  return (
    <div className="flex items-center gap-3 text-sm">
      <button disabled={page === 0} onClick={() => onChange(page - 1)} className="rounded border px-3 py-1 disabled:opacity-40">
        Previous
      </button>
      <span>Page {page + 1} of {totalPages}</span>
      <button disabled={page + 1 >= totalPages} onClick={() => onChange(page + 1)} className="rounded border px-3 py-1 disabled:opacity-40">
        Next
      </button>
    </div>
  )
}
