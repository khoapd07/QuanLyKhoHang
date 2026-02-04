import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '../layouts/AdminLayout.vue'
import Login from '../components/auth/Login.vue'
import Dashboard from '../components/dashboard/Dashboard.vue'

// Danh mục
import DMMay from '../components/danhmuc/DanhMucMay.vue'
import DonVi from '../components/danhmuc/DonVi.vue'
import Kho from '../components/danhmuc/KhoChiNhanh.vue'
import LoaiSP from '../components/danhmuc/LoaiSP.vue'
import Hang from '../components/danhmuc/HangSanXuat.vue'
import DMSanPham from '../components/danhmuc/DMSanPham.vue'

// Nghiệp vụ NHẬP
import NhapKho from '../components/nghiepvu/NhapKho.vue'
import NhapKhoTao from '../components/nghiepvu/NhapKhoTao.vue'

// Nghiệp vụ XUẤT
import XuatKho from '../components/nghiepvu/XuatKho.vue'
import XuatKhoTao from '../components/nghiepvu/XuatKhoTao.vue' 

// Nghiệp vụ CHUYỂN KHO (Mới thêm)
import ChuyenKho from '../components/nghiepvu/ChuyenKho.vue'
import ChuyenKhoTao from '../components/nghiepvu/ChuyenKhoTao.vue'

import BaoCaoTon from '../components/baocao/ThongKeTonKho.vue'

import TaiKhoan from '../components/admin/TaiKhoan.vue'

import BaoCaoTonDauNam from '../components/baocao/ChotThongKeDauNam.vue'


const routes = [
  { path: '/login', name: 'Login', component: Login },
  {
    path: '/',
    component: AdminLayout,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', name: 'Dashboard', component: Dashboard },
      
      // Danh mục
      { path: 'loai-san-pham', component: LoaiSP },
      { path: 'danh-muc-may', name: 'DMMay', component: DMMay },
      { path: 'don-vi', name: 'DonVi', component: DonVi },
      { path: 'kho', name: 'Kho', component: Kho },
      { path: 'hang-san-xuat', name: 'Hang', component: Hang },
      { path: 'danh-muc-san-pham', name: 'DMSanPham', component: DMSanPham },

      // Nhập kho
      { path: 'nhap-kho', name: 'NhapKho', component: NhapKho },
      { path: 'nhap-kho/tao-moi', name: 'TaoPhieuNhap', component: NhapKhoTao },

      // Xuất kho
      { path: 'xuat-kho', name: 'XuatKho', component: XuatKho },
      { path: 'xuat-kho/tao-moi', name: 'TaoPhieuXuat', component: XuatKhoTao }, 

      // Chuyển kho (Mới thêm)
      { path: 'chuyen-kho', name: 'ChuyenKho', component: ChuyenKho },
      { path: 'chuyen-kho/tao-moi', name: 'ChuyenKhoTao', component: ChuyenKhoTao },

      // Báo cáo & Hệ thống
      { path: 'bao-cao-ton', name: 'BaoCaoTon', component: BaoCaoTon },
      { path: 'bao-cao-ton-dau-nam', name: 'BaoCaoTonDauNam', component: BaoCaoTonDauNam },

      // 5. Hệ thống
      { path: 'tai-khoan', name: 'TaiKhoan', component: TaiKhoan }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  // 1. Lấy token từ LocalStorage
  const token = localStorage.getItem('token');
  
  // 2. Kiểm tra route cần bảo vệ (có meta: { requiresAuth: true })
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);

  if (requiresAuth && !token) {
    // Nếu cần login mà chưa có token -> Đá về trang Login
    next('/login');
  } else if (to.path === '/login' && token) {
    // Nếu đã có token mà cố vào trang Login -> Đá về Dashboard
    next('/dashboard');
  } else {
    // Cho phép đi tiếp
    next();
  }
})

export default router