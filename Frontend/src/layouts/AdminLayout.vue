<template>
  <div class="app-wrapper layout-fixed sidebar-expand-lg bg-body-tertiary">
    
    <nav class="app-header navbar navbar-expand bg-body">
      <div class="container-fluid">
        <ul class="navbar-nav">
          <li class="nav-item">
            <a class="nav-link" href="#" role="button" @click.prevent="toggleSidebar">
              <i class="bi bi-list"></i>
            </a>
          </li>
          <li class="nav-item d-none d-md-block">
            <a href="#" class="nav-link">Trang chủ</a>
          </li>
        </ul>

        <ul class="navbar-nav ms-auto">
          <li class="nav-item dropdown user-menu">
            <a href="#" class="nav-link dropdown-toggle" data-bs-toggle="dropdown">
              <img src="https://assets.minimals.cc/public/assets/images/mock/avatar/avatar-25.webp" class="user-image rounded-circle shadow" alt="User Image">
              <span class="d-none d-md-inline">Admin User</span>
            </a>
            <ul class="dropdown-menu dropdown-menu-lg dropdown-menu-end">
              <li class="user-header text-bg-primary">
                <img src="https://assets.minimals.cc/public/assets/images/mock/avatar/avatar-25.webp" class="rounded-circle shadow" alt="User Image">
                <p>
                  Quản lý kho
                  <small>Dự án 2026</small>
                </p>
              </li>
              <li class="user-footer">
                <a href="#" class="btn btn-default btn-flat">Hồ sơ</a>
                <a href="#" class="btn btn-default btn-flat float-end" @click="logout">Đăng xuất</a>
              </li>
            </ul>
          </li>
        </ul>
      </div>
    </nav>
    
    <aside class="app-sidebar bg-body-secondary shadow" data-bs-theme="dark">
      <div class="sidebar-brand">
        <a href="/" class="brand-link">
          <span class="brand-text fw-light">KHO MÁY IN</span>
        </a>
      </div>

      <div class="sidebar-wrapper">
        <nav class="mt-2">
          <ul class="nav sidebar-menu flex-column" data-lte-toggle="treeview" role="menu" data-accordion="false">
            
            <li class="nav-item">
              <router-link to="/dashboard" class="nav-link" active-class="active">
                <i class="nav-icon bi bi-speedometer2"></i>
                <p>Tổng quan</p>
              </router-link>
            </li>

            <li class="nav-header">DANH MỤC</li>

            <li class="nav-item">
              <router-link to="/hang-san-xuat" class="nav-link" active-class="active">
                <i class="nav-icon bi bi-box-seam"></i>
                <p>Hãng sản xuất</p>
              </router-link>
            </li>

            <li class="nav-item">
              <router-link to="/loai-san-pham" class="nav-link" active-class="active">
                <i class="nav-icon bi bi-box-seam"></i>
                <p>Loại sản phẩm</p>
              </router-link>
            </li>
            
            <li class="nav-item">
              <router-link to="/danh-muc-san-pham" class="nav-link" active-class="active">
                <i class="nav-icon bi bi-box-seam"></i>
                <p>Danh mục sản phẩm</p>
              </router-link>
            </li>

            <li class="nav-item">
              <router-link to="/danh-muc-may" class="nav-link" active-class="active">
                <i class="nav-icon bi bi-box-seam"></i>
                <p>Danh mục máy</p>
              </router-link>
            </li>



            <li class="nav-item">
              <router-link to="/don-vi" class="nav-link" active-class="active">
                <i class="nav-icon bi bi-building"></i>
                <p>Đơn vị (NCC/Khách)</p>
              </router-link>
            </li>

            <li class="nav-item">
              <router-link to="/kho" class="nav-link" active-class="active">
                <i class="nav-icon bi bi-house-door"></i>
                <p>Kho bãi</p>
              </router-link>
            </li>

            <li class="nav-header">NGHIỆP VỤ</li>
            <li class="nav-item menu-open" :class="{ 'menu-open': isKhoMenuOpen }">
              <a href="#" class="nav-link" @click.prevent="toggleKhoMenu">
                <i class="nav-icon bi bi-clipboard-data"></i>
                <p>
                  Quản lý Kho
                  <i class="nav-arrow bi bi-chevron-right"></i>
                </p>
              </a>
              <ul class="nav nav-treeview" :style="{ display: isKhoMenuOpen ? 'block' : 'none' }">
                <li class="nav-item">
                  <router-link to="/nhap-kho" class="nav-link" active-class="active">
                    <i class="nav-icon bi bi-arrow-down-square"></i>
                    <p>Nhập kho</p>
                  </router-link>
                </li>
                <li class="nav-item">
                  <router-link to="/xuat-kho" class="nav-link" active-class="active">
                    <i class="nav-icon bi bi-arrow-up-square"></i>
                    <p>Xuất kho</p>
                  </router-link>
                </li>
              </ul>
            </li>

            <li class="nav-header">BÁO CÁO</li>

              <li class="nav-item">
                <router-link to="/bao-cao-ton" class="nav-link" active-class="active">
                  <i class="nav-icon bi bi-pie-chart"></i>
                  <p>Tồn kho</p>
                </router-link>
              </li>

              <li class="nav-item">
                <router-link to="/bao-cao-ton-dau-nam" class="nav-link" active-class="active">
                  <i class="nav-icon bi bi-calendar-check-fill"></i> <p>Chốt sổ đầu năm</p>
                </router-link>
              </li>

             <li class="nav-header">HỆ THỐNG</li>
            <li class="nav-item">
              <router-link to="/tai-khoan" class="nav-link" active-class="active">
                <i class="nav-icon bi bi-people"></i>
                <p>Tài khoản</p>
              </router-link>
            </li>

          </ul>
        </nav>
      </div>
    </aside>

    <main class="app-main">
      <div class="app-content-header">
        <div class="container-fluid">
          <div class="row">
            <div class="col-sm-6">
              <h3 class="mb-0">Hệ thống quản lý</h3>
            </div>
          </div>
        </div>
      </div>
      
      <div class="app-content">
        <div class="container-fluid">
          <router-view></router-view>
        </div>
      </div>
    </main>

    <footer class="app-footer">
      <div class="float-end d-none d-sm-inline">Version 1.0</div>
      <strong>Copyright &copy; 2026 <a href="#">Kho Máy In</a>.</strong>
    </footer>

  </div>
</template>

<script setup>
// 1. THÊM 'ref' VÀO IMPORT
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 2. KHAI BÁO BIẾN TRẠNG THÁI (Bạn đang thiếu dòng này)
const isKhoMenuOpen = ref(false); // Mặc định là false (đóng)

// Hàm xử lý Đăng xuất
function logout() {
  localStorage.removeItem('token');
  router.push('/login');
}

// Hàm này bây giờ sẽ hoạt động vì biến isKhoMenuOpen đã được khai báo
function toggleKhoMenu() {
  isKhoMenuOpen.value = !isKhoMenuOpen.value;
}

// Hàm Toggle Sidebar thủ công
function toggleSidebar() {
  const body = document.querySelector('body');
  if (window.innerWidth >= 992) {
    body.classList.toggle('sidebar-collapse');
  } else {
    body.classList.toggle('sidebar-open');
  }
}

onMounted(() => {
  // Logic khởi tạo nếu cần
});
</script>

<style scoped>
.app-wrapper {
  min-height: 100vh;
}
</style>