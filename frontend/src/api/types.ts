import type { components as Catalog } from './schema/catalog'
import type { components as Customer } from './schema/customer'
import type { components as Integration } from './schema/integration'
import type { components as Order } from './schema/order'

export type Product = Catalog['schemas']['ProductResponse']
export type Review = Catalog['schemas']['ReviewResponse']
export type Address = Customer['schemas']['AddressResponse']
export type AddressInput = Customer['schemas']['AddressRequest']
export type CartItem = Order['schemas']['CartItem']
export type OrderDetails = Order['schemas']['OrderResponse']
export type ShippingAddress = Order['schemas']['ShippingAddress']
export type Payment = Integration['schemas']['PaymentResponse']
export type Shipment = Integration['schemas']['ShipmentResponse']
