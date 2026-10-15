export class ApiError extends Error {
  readonly status: number

  constructor(message: string, status: number) {
    super(message)
    this.status = status
  }
}

type FetchResult<T> = { data?: T; error?: unknown; response: Response }

export async function call<T>(request: Promise<FetchResult<T>>): Promise<T> {
  const { data, error, response } = await request
  if (!response.ok) {
    const message = (error as { message?: string } | undefined)?.message ?? `Request failed (${response.status})`
    throw new ApiError(message, response.status)
  }
  return data as T
}

export async function callOrNull<T>(request: Promise<FetchResult<T>>): Promise<T | null> {
  try {
    return await call(request)
  } catch (e) {
    if (e instanceof ApiError && e.status === 404) {
      return null
    }
    throw e
  }
}
