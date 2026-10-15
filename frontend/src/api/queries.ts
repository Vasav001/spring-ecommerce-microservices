import { useQuery } from '@tanstack/react-query'
import { call } from './call'
import { catalogApi, customerApi, orderApi } from './client'
import type { Product } from './types'

export function useCart(enabled = true) {
  return useQuery({
    queryKey: ['cart'],
    enabled,
    queryFn: () => call(orderApi.GET('/api/v1/cart')),
  })
}

export function useProducts(ids: string[]) {
  return useQuery({
    queryKey: ['products', [...ids].sort()],
    enabled: ids.length > 0,
    queryFn: async () => {
      const products = await call(catalogApi.GET('/api/v1/products/lookup', { params: { query: { ids } } }))
      return new Map<string, Product>(products.map((p) => [p.id ?? '', p]))
    },
  })
}

export function useProfile(enabled = true) {
  return useQuery({
    queryKey: ['profile'],
    enabled,
    queryFn: () => call(customerApi.GET('/api/v1/customers/me')),
  })
}
