import { useState, type ChangeEvent } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { call } from '../api/call'
import { customerApi, integrationApi } from '../api/client'
import { useProfile } from '../api/queries'
import type { Address, AddressInput } from '../api/types'
import { useSession } from '../auth/session'
import AddressForm from '../components/AddressForm'
import { uploadImage } from '../lib/cloudinary'

function toInput(address: Address): AddressInput {
  return {
    name: address.name ?? '',
    phone: address.phone ?? '',
    line1: address.line1 ?? '',
    line2: address.line2 ?? '',
    city: address.city ?? '',
    state: address.state ?? '',
    postalCode: address.postalCode ?? '',
    country: address.country ?? '',
  }
}

export default function ProfilePage() {
  const session = useSession()
  const profile = useProfile()

  if (profile.isPending) {
    return <p className="text-slate-500">Loading…</p>
  }
  if (profile.isError) {
    return <p className="text-red-600">{profile.error.message}</p>
  }

  return (
    <div className="space-y-10">
      <h1 className="text-2xl font-semibold">Profile</h1>
      <PictureSection name={session.name} pictureUrl={profile.data.profilePictureUrl} />
      <AddressesSection addresses={profile.data.addresses ?? []} />
    </div>
  )
}

function PictureSection({ name, pictureUrl }: { name?: string; pictureUrl?: string }) {
  const queryClient = useQueryClient()
  const refresh = () => queryClient.invalidateQueries({ queryKey: ['profile'] })

  const upload = useMutation({
    mutationFn: async (file: File) => {
      const url = await uploadImage(file, () => call(integrationApi.POST('/api/v1/uploads/signature')))
      return call(customerApi.PUT('/api/v1/customers/me', { body: { profilePictureUrl: url } }))
    },
    onSuccess: refresh,
  })
  const remove = useMutation({
    mutationFn: () => call(customerApi.PUT('/api/v1/customers/me', { body: {} })),
    onSuccess: refresh,
  })

  const onFile = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      upload.mutate(file)
    }
    e.target.value = ''
  }

  const busy = upload.isPending || remove.isPending
  const error = upload.error ?? remove.error

  return (
    <section className="flex flex-wrap items-center gap-6">
      {pictureUrl
        ? <img src={pictureUrl} alt="" className="h-24 w-24 rounded-full object-cover" />
        : <div className="flex h-24 w-24 items-center justify-center rounded-full bg-slate-200 text-2xl text-slate-500">
            {(name ?? '?').charAt(0).toUpperCase()}
          </div>}
      <div className="space-y-2">
        <p className="font-medium">{name}</p>
        <div className="flex flex-wrap gap-2 text-sm">
          <label className={`cursor-pointer rounded border border-slate-300 px-3 py-1 hover:bg-slate-100 ${busy ? 'pointer-events-none opacity-50' : ''}`}>
            {upload.isPending ? 'Uploading…' : pictureUrl ? 'Change picture' : 'Upload picture'}
            <input type="file" accept="image/*" onChange={onFile} className="hidden" />
          </label>
          {pictureUrl && (
            <button disabled={busy} onClick={() => remove.mutate()} className="rounded border border-slate-300 px-3 py-1 hover:bg-slate-100 disabled:opacity-50">
              Remove
            </button>
          )}
        </div>
        {error && <p className="text-sm text-red-600">{error.message}</p>}
      </div>
    </section>
  )
}

function AddressesSection({ addresses }: { addresses: Address[] }) {
  const queryClient = useQueryClient()
  const [editing, setEditing] = useState<string | 'new' | null>(null)
  const refresh = async () => {
    await queryClient.invalidateQueries({ queryKey: ['profile'] })
    setEditing(null)
  }

  const add = useMutation({
    mutationFn: (address: AddressInput) => call(customerApi.POST('/api/v1/customers/me/addresses', { body: address })),
    onSuccess: refresh,
  })
  const update = useMutation({
    mutationFn: ({ addressId, address }: { addressId: string; address: AddressInput }) =>
      call(customerApi.PUT('/api/v1/customers/me/addresses/{addressId}', { params: { path: { addressId } }, body: address })),
    onSuccess: refresh,
  })
  const remove = useMutation({
    mutationFn: (addressId: string) =>
      call(customerApi.DELETE('/api/v1/customers/me/addresses/{addressId}', { params: { path: { addressId } } })),
    onSuccess: refresh,
  })

  return (
    <section className="space-y-4">
      <h2 className="text-lg font-semibold">Addresses</h2>
      {addresses.length === 0 && editing !== 'new' && <p className="text-slate-500">No saved addresses yet.</p>}
      <ul className="space-y-3">
        {addresses.map((address) => (
          <li key={address.id} className="rounded border border-slate-200 bg-white p-4">
            {editing === address.id ? (
              <AddressForm
                initial={toInput(address)}
                submitLabel="Save changes"
                submitting={update.isPending}
                error={update.error?.message}
                onSubmit={(input) => update.mutate({ addressId: address.id ?? '', address: input })}
                onCancel={() => setEditing(null)}
              />
            ) : (
              <div className="flex flex-wrap items-start gap-4">
                <p className="flex-1 text-sm">
                  <span className="font-medium">{address.name}</span> · {address.phone}<br />
                  {address.line1}{address.line2 ? `, ${address.line2}` : ''}<br />
                  {address.city}, {address.state} {address.postalCode}, {address.country}
                </p>
                <button onClick={() => setEditing(address.id ?? null)} className="text-sm text-blue-700 hover:underline">Edit</button>
                <button
                  disabled={remove.isPending}
                  onClick={() => remove.mutate(address.id ?? '')}
                  className="text-sm text-red-600 hover:underline disabled:opacity-50"
                >
                  Delete
                </button>
              </div>
            )}
          </li>
        ))}
      </ul>
      {remove.isError && <p className="text-sm text-red-600">{remove.error.message}</p>}
      {editing === 'new' ? (
        <AddressForm
          submitLabel="Add address"
          submitting={add.isPending}
          error={add.error?.message}
          onSubmit={(input) => add.mutate(input)}
          onCancel={() => setEditing(null)}
        />
      ) : (
        <button onClick={() => setEditing('new')} className="text-sm text-blue-700 hover:underline">+ Add a new address</button>
      )}
    </section>
  )
}
