import type { components as Integration } from '../api/schema/integration'

type UploadSignature = Integration['schemas']['UploadSignatureResponse']

const MAX_BYTES = 5 * 1024 * 1024
const MOCK_IMAGE_URL = 'https://res.cloudinary.com/demo/image/upload/sample.jpg'
const mockUploads = import.meta.env.VITE_MOCK_UPLOADS === 'true'

export async function uploadImage(file: File, getSignature: () => Promise<UploadSignature>): Promise<string> {
  if (!file.type.startsWith('image/')) {
    throw new Error('Please choose an image file')
  }
  if (file.size > MAX_BYTES) {
    throw new Error('Images must be 5 MB or smaller')
  }
  if (mockUploads) {
    return MOCK_IMAGE_URL
  }
  const signature = await getSignature()
  const form = new FormData()
  form.append('file', file)
  form.append('api_key', signature.apiKey ?? '')
  form.append('timestamp', String(signature.timestamp))
  form.append('folder', signature.folder ?? '')
  form.append('signature', signature.signature ?? '')
  const response = await fetch(signature.uploadUrl ?? '', { method: 'POST', body: form })
  const body = await response.json()
  if (!response.ok) {
    throw new Error(body?.error?.message ?? 'Upload failed')
  }
  return body.secure_url as string
}
