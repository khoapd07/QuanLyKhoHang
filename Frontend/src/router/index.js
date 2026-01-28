import { createRouter, createWebHistory } from 'vue-router'

// =========================================================
// 1. IMPORT LAYOUT (Khung giao diện chung)
// =========================================================
import AdminLayout from '../layouts/AdminLayout.vue'

// =========================================================
// 2. IMPORT COMPONENTS (Các màn hình chức năng)
// =========================================================

// --- A. Authentication ---
import Login from '../components/auth/Login.vue'

// --- B. Dashboard ---
import Dashboard from '../components/dashboard/Dashboard.vue'

// --- C. Danh mục (Master Data) ---
import SanPham from '../components/danhmuc/DanhMucMay.vue'
import DonVi from '../components/danhmuc/DonVi.vue'    // Gộp Nhà cung cấp & Khách hàng
import Kho from '../components/danhmuc/KhoChiNhanh.vue'        // Danh sách kho bãi

import Hang from '../components/danhmuc/HangSanXuat.vue'        // Danh sách hãng sản xuất (Không dùng đến, giữ lại để mở rộng sau này)

// --- D. Nghiệp vụ (Business Logic) ---
import NhapKho from '../components/nghiepvu/NhapKho.vue'
import NhapKhoChiTiet from '../components/nghiepvu/NhapKhoChiTiet.vue' // Mở comment khi làm chức năng thêm mới

import XuatKho from '../components/nghiepvu/XuatKho.vue'
import XuatKhoChiTiet from '../components/nghiepvu/XuatKhoChiTiet.vue' // Mở comment khi làm chức năng thêm mới

// --- E. Báo cáo (Report) ---
import BaoCaoTon from '../components/baocao/ThongKeTonKho.vue'

// --- F. Hệ thống (Admin) ---
import TaiKhoan from '../components/admin/TaiKhoan.vue' // Quản lý người dùng

// =========================================================
// 3. ĐỊNH NGHĨA ROUTES
// =========================================================
const routes = [
  // ➤ Route Public (Không cần đăng nhập)
  {
    path: '/login',
    name: 'Login',
    component: Login
  },

  // ➤ Route Private (Cần đăng nhập & có AdminLayout)
  {
    path: '/',
    component: AdminLayout,
    meta: { requiresAuth: true },
    children: [
      // Mặc định vào Dashboard
      { path: '', redirect: '/dashboard' },
      
      // 1. Dashboard
      { path: 'dashboard', name: 'Dashboard', component: Dashboard },

      // 2. Danh mục
      { path: 'san-pham', name: 'SanPham', component: SanPham },
      { path: 'don-vi', name: 'DonVi', component: DonVi },
      { path: 'kho', name: 'Kho', component: Kho },

      // 3. Nghiệp vụ
      { path: 'nhap-kho', name: 'NhapKho', component: NhapKho },
      { path: 'nhap-kho/tao-moi', name: 'TaoPhieuNhap', component: NhapKhoChiTiet },

      { path: 'xuat-kho', name: 'XuatKho', component: XuatKho },
      { path: 'xuat-kho/tao-moi', name: 'TaoPhieuXuat', component: XuatKhoChiTiet },

      // 4. Báo cáo
      { path: 'bao-cao-ton', name: 'BaoCaoTon', component: BaoCaoTon },

      // 5. Hệ thống
      { path: 'tai-khoan', name: 'TaiKhoan', component: TaiKhoan }
    ]
  }
]

// =========================================================
// 4. KHỞI TẠO ROUTER
// =========================================================
const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation Guard: Kiểm tra Token trước khi vào trang
router.beforeEach((to, from, next) => {
  // TẠM THỜI: Cho phép tất cả mọi người đi qua, không cần kiểm tra token
  // const token = localStorage.getItem('token');
  // if (to.meta.requiresAuth && !token) {
  //   next('/login');
  // } else {
  //   next();
  // }
  
  console.log("Dev Mode: Bypassing Auth check");
  next();
})

export default router