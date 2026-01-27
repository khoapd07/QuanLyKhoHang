import { createRouter, createWebHistory } from 'vue-router'

// --- 1. IMPORT LAYOUT ---
import AdminLayout from '../layouts/AdminLayout.vue'

// --- 2. IMPORT CÁC COMPONENTS (PAGES) ---
// Auth
import Login from '../components/auth/Login.vue'

// Dashboard
import Dashboard from '../components/dashboard/Dashboard.vue'

// Danh mục
import SanPham from '../components/danhmuc/SanPham.vue'
import NhaCungCap from '../components/danhmuc/NhaCungCap.vue'
import KhachHang from '../components/danhmuc/KhachHang.vue'
import Kho from '../components/danhmuc/Kho.vue'

// Nghiệp vụ
import NhapKho from '../components/nghiepvu/NhapKho.vue'
import NhapKhoChiTiet from '../components/nghiepvu/NhapKhoChiTiet.vue' // Form nhập
import XuatKho from '../components/nghiepvu/XuatKho.vue'
import XuatKhoChiTiet from '../components/nghiepvu/XuatKhoChiTiet.vue' // Form xuất

// Báo cáo
import BaoCaoTon from '../components/baocao/BaoCaoTon.vue'

// --- 3. ĐỊNH NGHĨA ROUTES ---
const routes = [
  // A. TRANG LOGIN (Không dính dáng đến AdminLayout)
  {
    path: '/login',
    name: 'Login',
    component: Login
  },

  // B. KHU VỰC ADMIN (Có Sidebar, Header)
  {
    path: '/',
    component: AdminLayout,
    meta: { requiresAuth: true }, // Đánh dấu cần đăng nhập
    children: [
      // 1. Dashboard (Mặc định vào đây)
      { path: '', redirect: '/dashboard' },
      { 
        path: 'dashboard', 
        name: 'Dashboard', 
        component: Dashboard 
      },

      // 2. Danh mục
      { path: 'san-pham', name: 'SanPham', component: SanPham },
      { path: 'nha-cung-cap', name: 'NhaCungCap', component: NhaCungCap },
      { path: 'khach-hang', name: 'KhachHang', component: KhachHang },
      { path: 'danh-sach-kho', name: 'Kho', component: Kho },

      // 3. Nghiệp vụ Nhập Kho
      { path: 'nhap-kho', name: 'NhapKho', component: NhapKho },
      { path: 'nhap-kho/tao-moi', name: 'TaoPhieuNhap', component: NhapKhoChiTiet },
      // Route chỉnh sửa phiếu nhập (nếu cần sau này)
      { path: 'nhap-kho/sua/:soPhieu', name: 'SuaPhieuNhap', component: NhapKhoChiTiet },

      // 4. Nghiệp vụ Xuất Kho
      { path: 'xuat-kho', name: 'XuatKho', component: XuatKho },
      { path: 'xuat-kho/tao-moi', name: 'TaoPhieuXuat', component: XuatKhoChiTiet },

      // 5. Báo cáo
      { path: 'bao-cao-ton', name: 'BaoCaoTon', component: BaoCaoTon }
    ]
  },
  
  // Route bắt lỗi (404) - Tùy chọn
  // { path: '/:pathMatch(.*)*', redirect: '/login' }
]

// --- 4. KHỞI TẠO ROUTER ---
const router = createRouter({
  history: createWebHistory(),
  routes
})

// --- 5. KIỂM TRA ĐĂNG NHẬP (Navigation Guard) ---
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token'); // Lấy token từ bộ nhớ
  
  // Nếu trang yêu cầu login mà chưa có token -> Đá về trang Login
  if (to.meta.requiresAuth && !token) {
    next('/login');
  } else {
    next();
  }
})

export default router