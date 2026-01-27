import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '../layouts/AdminLayout.vue'
import Dashboard from '../components/Dashboard.vue'
// import Login from '../components/Login.vue' // Bạn sẽ cần tạo file này

const routes = [
//   { path: '/login', component: Login },
  { 
    path: '/', 
    component: AdminLayout, // Layout dùng chung là cha
    children: [
      { path: '', redirect: '/dashboard' }, // Vào trang chủ tự nhảy sang dashboard
      { path: 'dashboard', component: Dashboard }, // Dashboard nằm trong Layout
      // Sau này thêm: { path: 'san-pham', component: SanPham }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router