<template>
  <div class="app-wrapper layout-fixed sidebar-expand-lg bg-body-tertiary">
    
    <nav class="app-header navbar navbar-expand bg-body">
      <div class="container-fluid">
        <ul class="navbar-nav">
          <li class="nav-item">
            <a class="nav-link" data-lte-toggle="sidebar" href="#" role="button">
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
                <p>Tổng quan (Dashboard)</p>
              </router-link>
            </li>

            <li class="nav-header">QUẢN LÝ</li>
            
            <li class="nav-item">
              <router-link to="/san-pham" class="nav-link" active-class="active">
                <i class="nav-icon bi bi-box-seam"></i>
                <p>Sản phẩm</p>
              </router-link>
            </li>

            <li class="nav-item"> <a href="#" class="nav-link">
                <i class="nav-icon bi bi-clipboard-data"></i>
                <p>
                  Nghiệp vụ Kho
                  <i class="nav-arrow bi bi-chevron-right"></i>
                </p>
              </a>
              <ul class="nav nav-treeview">
                <li class="nav-item">
                  <router-link to="/nhap-kho" class="nav-link" active-class="active">
                    <i class="nav-icon bi bi-circle"></i>
                    <p>Nhập kho</p>
                  </router-link>
                </li>
                <li class="nav-item">
                  <router-link to="/xuat-kho" class="nav-link" active-class="active">
                    <i class="nav-icon bi bi-circle"></i>
                    <p>Xuất kho</p>
                  </router-link>
                </li>
              </ul>
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

// AdminLTE v4 dùng data-attribute để toggle sidebar, 
// nhưng trong Vue đôi khi cần kích hoạt lại các script UI khi component load
onMounted(() => {
  // Nếu cần thiết, có thể gọi hàm khởi tạo UI của AdminLTE tại đây
  // Thường thì import js ở main.js là đủ.
});
</script>

<style>
/* Đè lại một chút style nếu cần */
.app-wrapper {
  min-height: 100vh;
}
</style>