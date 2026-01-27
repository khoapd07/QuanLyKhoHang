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
                  Admin User - Quản lý Kho
                  <small>Thành viên từ 2024</small>
                </p>
              </li>
              <li class="user-footer">
                <a href="#" class="btn btn-default btn-flat">Hồ sơ</a>
                <a href="#" class="btn btn-default btn-flat float-end">Đăng xuất</a>
              </li>
            </ul>
          </li>
        </ul>
      </div>
    </nav>
    
    <aside class="app-sidebar bg-body-secondary shadow" data-bs-theme="dark">
      <div class="sidebar-brand">
        <a href="/" class="brand-link">
          <span class="brand-text fw-light">QUẢN LÝ KHO</span>
        </a>
      </div>

      <div class="sidebar-wrapper">
        <nav class="mt-2">
          <ul class="nav sidebar-menu flex-column" data-lte-toggle="treeview" role="menu" data-accordion="false">
            
            <li class="nav-item">
              <router-link to="/dashboard" class="nav-link" active-class="active">
                <i class="nav-icon bi bi-speedometer2"></i>
                <p>Dashboard</p>
              </router-link>
            </li>

            <li class="nav-header">DANH MỤC</li>
            
            <li class="nav-item">
              <router-link to="/san-pham" class="nav-link" active-class="active">
                <i class="nav-icon bi bi-box-seam"></i>
                <p>Sản phẩm</p>
              </router-link>
            </li>

            <li class="nav-item">
              <router-link to="/nha-cung-cap" class="nav-link" active-class="active">
                <i class="nav-icon bi bi-truck"></i>
                <p>Nhà cung cấp</p>
              </router-link>
            </li>

            <li class="nav-item">
              <router-link to="/khach-hang" class="nav-link" active-class="active">
                <i class="nav-icon bi bi-people"></i>
                <p>Khách hàng</p>
              </router-link>
            </li>

            <li class="nav-item">
              <router-link to="/danh-sach-kho" class="nav-link" active-class="active">
                <i class="nav-icon bi bi-house-door"></i>
                <p>Danh sách Kho</p>
              </router-link>
            </li>

            <li class="nav-header">NGHIỆP VỤ</li>

            <li class="nav-item menu-open"> 
              <a href="#" class="nav-link">
                <i class="nav-icon bi bi-clipboard-data"></i>
                <p>
                  Quản lý Kho
                  <i class="nav-arrow bi bi-chevron-right"></i>
                </p>
              </a>
              <ul class="nav nav-treeview">
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
                <p>Báo cáo Tồn kho</p>
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
              <h3 class="mb-0">Hệ thống</h3>
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
import { onMounted } from 'vue';

// Hàm Toggle Sidebar thủ công (Fix lỗi không nhận JS của AdminLTE)
function toggleSidebar() {
  const body = document.querySelector('body');
  if (window.innerWidth >= 992) {
    body.classList.toggle('sidebar-collapse');
  } else {
    body.classList.toggle('sidebar-open');
  }
}

onMounted(() => {
  // Có thể thêm logic khởi tạo nếu cần
});
</script>

<style>
.app-wrapper {
  min-height: 100vh;
}
</style>