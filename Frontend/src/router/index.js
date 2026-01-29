import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '../layouts/AdminLayout.vue'
import Login from '../components/auth/Login.vue'
import Dashboard from '../components/dashboard/Dashboard.vue'

// Danh mục
import SanPham from '../components/danhmuc/DanhMucMay.vue'
import DonVi from '../components/danhmuc/DonVi.vue'
import Kho from '../components/danhmuc/KhoChiNhanh.vue'
import LoaiSP from '../components/danhmuc/LoaiSP.vue'
import Hang from '../components/danhmuc/HangSanXuat.vue'

// Nghiệp vụ NHẬP
import NhapKho from '../components/nghiepvu/NhapKho.vue'
import NhapKhoTao from '../components/nghiepvu/NhapKhoTao.vue'

// Nghiệp vụ XUẤT
import XuatKho from '../components/nghiepvu/XuatKho.vue'
import XuatKhoTao from '../components/nghiepvu/XuatKhoTao.vue' // [MỚI] Trang tạo phiếu xuất

import BaoCaoTon from '../components/baocao/ThongKeTonKho.vue'
import TaiKhoan from '../components/admin/TaiKhoan.vue'

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
      { path: 'san-pham', name: 'SanPham', component: SanPham },
      { path: 'don-vi', name: 'DonVi', component: DonVi },
      { path: 'kho', name: 'Kho', component: Kho },
      { path: 'hang-san-xuat', name: 'Hang', component: Hang },

      // Nhập kho
      { path: 'nhap-kho', name: 'NhapKho', component: NhapKho },
      { path: 'nhap-kho/tao-moi', name: 'TaoPhieuNhap', component: NhapKhoTao },

      // Xuất kho
      { path: 'xuat-kho', name: 'XuatKho', component: XuatKho },
      { path: 'xuat-kho/tao-moi', name: 'TaoPhieuXuat', component: XuatKhoTao }, // Trỏ vào file Tao

      // Báo cáo & Hệ thống
      { path: 'bao-cao-ton', name: 'BaoCaoTon', component: BaoCaoTon },
      { path: 'tai-khoan', name: 'TaiKhoan', component: TaiKhoan }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  console.log("Dev Mode: Bypassing Auth check");
  next();
})

export default router