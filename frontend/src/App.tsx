import { Navigate, Route, Routes } from 'react-router'
import Layout from './components/Layout'
import RequireAuth from './components/RequireAuth'
import AdminInventoryPage from './pages/admin/AdminInventoryPage'
import AdminLayout from './pages/admin/AdminLayout'
import AdminOrderPage from './pages/admin/AdminOrderPage'
import AdminOrdersPage from './pages/admin/AdminOrdersPage'
import AdminProductEditPage from './pages/admin/AdminProductEditPage'
import AdminProductsPage from './pages/admin/AdminProductsPage'
import CartPage from './pages/CartPage'
import CheckoutPage from './pages/CheckoutPage'
import OrderPage from './pages/OrderPage'
import OrdersPage from './pages/OrdersPage'
import Placeholder from './pages/Placeholder'
import ProductPage from './pages/ProductPage'
import ProfilePage from './pages/ProfilePage'
import SearchPage from './pages/SearchPage'

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<SearchPage />} />
        <Route path="products/:id" element={<ProductPage />} />
        <Route path="cart" element={<RequireAuth><CartPage /></RequireAuth>} />
        <Route path="checkout" element={<RequireAuth><CheckoutPage /></RequireAuth>} />
        <Route path="orders" element={<RequireAuth><OrdersPage /></RequireAuth>} />
        <Route path="orders/:id" element={<RequireAuth><OrderPage /></RequireAuth>} />
        <Route path="profile" element={<RequireAuth><ProfilePage /></RequireAuth>} />
        <Route path="admin" element={<RequireAuth admin><AdminLayout /></RequireAuth>}>
          <Route index element={<Navigate to="products" replace />} />
          <Route path="products" element={<AdminProductsPage />} />
          <Route path="products/new" element={<AdminProductEditPage />} />
          <Route path="products/:id" element={<AdminProductEditPage />} />
          <Route path="inventory" element={<AdminInventoryPage />} />
          <Route path="orders" element={<AdminOrdersPage />} />
          <Route path="orders/:id" element={<AdminOrderPage />} />
        </Route>
        <Route path="*" element={<Placeholder title="Page not found" />} />
      </Route>
    </Routes>
  )
}
