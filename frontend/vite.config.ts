import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

type Service = 'customer' | 'catalog' | 'inventory' | 'order' | 'search' | 'integration'

const routes: Record<Service, string[]> = {
  customer: ['/api/v1/customers'],
  catalog: ['/api/v1/products', '/api/v1/reviews', '/api/v1/admin/products'],
  inventory: ['/api/v1/inventory', '/api/v1/admin/inventory'],
  order: ['/api/v1/cart', '/api/v1/checkout', '/api/v1/orders', '/api/v1/admin/orders'],
  search: ['/api/v1/search'],
  integration: ['/api/v1/payments', '/api/v1/shipments', '/api/v1/uploads', '/api/v1/admin/shipments', '/api/v1/admin/uploads'],
}

const ports: Record<'real' | 'mock', Record<Service, number>> = {
  real: { customer: 8081, catalog: 8082, inventory: 8083, order: 8084, search: 8085, integration: 8086 },
  mock: { customer: 4011, catalog: 4012, inventory: 4013, order: 4014, search: 4015, integration: 4016 },
}

export default defineConfig(({ mode }) => {
  const target = mode === 'mock' ? ports.mock : ports.real
  const proxy = Object.fromEntries(
    (Object.entries(routes) as [Service, string[]][]).flatMap(([service, prefixes]) =>
      prefixes.map((prefix) => [prefix, `http://localhost:${target[service]}`])),
  )
  return {
    plugins: [react(), tailwindcss()],
    server: { port: 5173, strictPort: true, proxy },
  }
})
