import { useState, type FormEvent } from 'react'
import type { AddressInput } from '../api/types'

const empty: AddressInput = { name: '', phone: '', line1: '', line2: '', city: '', state: '', postalCode: '', country: 'IN' }

const fields: { key: keyof AddressInput; label: string; optional?: boolean }[] = [
  { key: 'name', label: 'Full name' },
  { key: 'phone', label: 'Phone' },
  { key: 'line1', label: 'Address line 1' },
  { key: 'line2', label: 'Address line 2', optional: true },
  { key: 'city', label: 'City' },
  { key: 'state', label: 'State' },
  { key: 'postalCode', label: 'Postal code' },
  { key: 'country', label: 'Country (2-letter code)' },
]

type Props = {
  initial?: AddressInput
  submitLabel: string
  submitting: boolean
  error?: string
  onSubmit: (address: AddressInput) => void
  onCancel?: () => void
}

export default function AddressForm({ initial, submitLabel, submitting, error, onSubmit, onCancel }: Props) {
  const [values, setValues] = useState<AddressInput>(initial ?? empty)

  const submit = (e: FormEvent) => {
    e.preventDefault()
    onSubmit({ ...values, line2: values.line2 || undefined, country: values.country.toUpperCase() })
  }

  return (
    <form onSubmit={submit} className="grid max-w-xl grid-cols-1 gap-3 sm:grid-cols-2">
      {fields.map((field) => (
        <label key={field.key} className={`text-sm ${field.key === 'line1' || field.key === 'line2' ? 'sm:col-span-2' : ''}`}>
          <span className="text-slate-600">{field.label}</span>
          <input
            value={values[field.key] ?? ''}
            required={!field.optional}
            onChange={(e) => setValues({ ...values, [field.key]: e.target.value })}
            className="mt-1 w-full rounded border border-slate-300 px-3 py-2"
          />
        </label>
      ))}
      {error && <p className="text-sm text-red-600 sm:col-span-2">{error}</p>}
      <div className="flex gap-2 sm:col-span-2">
        <button disabled={submitting} className="rounded bg-slate-900 px-4 py-2 text-sm text-white hover:bg-slate-700 disabled:opacity-50">
          {submitting ? 'Saving…' : submitLabel}
        </button>
        {onCancel && (
          <button type="button" onClick={onCancel} className="rounded border border-slate-300 px-4 py-2 text-sm hover:bg-slate-100">
            Cancel
          </button>
        )}
      </div>
    </form>
  )
}
