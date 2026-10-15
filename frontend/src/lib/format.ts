const currency = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' })

export function formatPrice(value: number | undefined) {
  return value === undefined ? '—' : currency.format(value)
}

export function formatDate(value: string | undefined) {
  return value ? new Date(value).toLocaleString() : '—'
}
