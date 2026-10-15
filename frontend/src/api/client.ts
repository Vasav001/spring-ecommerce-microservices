import createClient, { type Middleware } from 'openapi-fetch'
import type { paths as CatalogPaths } from './schema/catalog'
import type { paths as CustomerPaths } from './schema/customer'
import type { paths as IntegrationPaths } from './schema/integration'
import type { paths as InventoryPaths } from './schema/inventory'
import type { paths as OrderPaths } from './schema/order'
import type { paths as SearchPaths } from './schema/search'

let accessToken: string | undefined

export function setAccessToken(token: string | undefined) {
  accessToken = token
}

const auth: Middleware = {
  onRequest({ request }) {
    if (accessToken) {
      request.headers.set('Authorization', `Bearer ${accessToken}`)
    }
    return request
  },
}

function api<Paths extends object>() {
  const client = createClient<Paths>()
  client.use(auth)
  return client
}

export const customerApi = api<CustomerPaths>()
export const catalogApi = api<CatalogPaths>()
export const inventoryApi = api<InventoryPaths>()
export const orderApi = api<OrderPaths>()
export const searchApi = api<SearchPaths>()
export const integrationApi = api<IntegrationPaths>()
