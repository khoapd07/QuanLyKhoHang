<template>
  <div class="app-wrapper layout-fixed sidebar-expand-lg bg-body-tertiary sidebar-mini">
    
    <nav class="app-header navbar navbar-expand bg-body shadow-sm">
      <div class="container-fluid">
        <ul class="navbar-nav">
          <!-- <li class="nav-item d-lg-none">
            <a class="nav-link" href="#" role="button" @click.prevent="toggleSidebar">
              <i class="bi bi-list fs-3"></i>
            </a>
          </li> -->
          
          <li class="nav-item d-none d-md-block">
            <a href="#" class="nav-link">Trang chủ</a>
          </li>
        </ul>

        <ul class="navbar-nav ms-auto">
          <li class="nav-item dropdown user-menu">
            <a href="#" class="nav-link dropdown-toggle d-flex align-items-center gap-2" data-bs-toggle="dropdown">
              <img src="https://assets.minimals.cc/public/assets/images/mock/avatar/avatar-25.webp" class="user-image rounded-circle shadow-sm" alt="User Image">
              <span class="d-none d-md-inline">{{ currentUser.name }}</span>
            </a>
            <ul class="dropdown-menu dropdown-menu-lg dropdown-menu-end">
              <li class="user-header text-bg-primary">
                <img src="https://assets.minimals.cc/public/assets/images/mock/avatar/avatar-25.webp" class="rounded-circle shadow" alt="User Image">
                <p>
                  {{ currentUser.name }}
                  <small>{{ currentUser.role }} - Dự án 2026</small>
                </p>
              </li>
              <li class="user-footer">
                <a href="#" class="btn btn-default btn-flat">Hồ sơ</a>
                <a href="#" class="btn btn-default btn-flat float-end" @click.prevent="logout">Đăng xuất</a>
              </li>
            </ul>
          </li>
        </ul>
      </div>
    </nav>
    
    <aside class="app-sidebar bg-body-secondary shadow" data-bs-theme="dark">
      
      <div class="sidebar-brand d-flex justify-content-between align-items-center px-3">
        
        <a href="/" class="brand-link text-decoration-none p-0">
          <span class="brand-text fw-bold text-uppercase text-white">KHO MÁY IN</span>
        </a>

        <a href="#" class="text-white opacity-75 hover-opacity-100 sidebar-toggle-btn" @click.prevent="toggleSidebar" title="Thu gọn/Mở rộng">
          <i class="bi bi-list fs-4"></i>
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
                <i class="nav-icon bi bi-tags"></i>
                <p>Loại sản phẩm</p>
              </router-link>
            </li>
            <li class="nav-item">
              <router-link to="/danh-muc-san-pham" class="nav-link" active-class="active">
                <i class="nav-icon bi bi-qr-code"></i>
                <p>Danh mục sản phẩm</p>
              </router-link>
            </li>
            <li class="nav-item">
              <router-link to="/danh-muc-may" class="nav-link" active-class="active">
                <i class="nav-icon bi bi-upc-scan"></i>
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
              <router-link to="/kho" class="nav-link" active-class="active" v-if="isAdmin">
                <i class="nav-icon bi bi-house-door"></i>
                <p>Kho bãi</p>
              </router-link>
            </li>

            <li class="nav-header">NGHIỆP VỤ</li>
            <li class="nav-item" :class="{ 'menu-open': isKhoMenuOpen }">
              <a href="#" class="nav-link" @click.prevent="toggleKhoMenu">
                <i class="nav-icon bi bi-clipboard-data"></i>
                <p>
                  Quản lý Kho
                  <i class="nav-arrow bi bi-chevron-right float-end"></i>
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
                <li class="nav-item">
                  <router-link to="/chuyen-kho" class="nav-link" active-class="active">
                    <i class="nav-icon bi bi-arrow-left-right"></i>
                    <p>Chuyển kho</p>
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
                <i class="nav-icon bi bi-calendar-check"></i> 
                <p>Chốt sổ đầu năm</p>
              </router-link>
            </li>

            <li class="nav-header" v-if="isAdmin">HỆ THỐNG</li>
            <li class="nav-item" v-if="isAdmin">
              <router-link to="/tai-khoan" class="nav-link" active-class="active">
                <i class="nav-icon bi bi-people-fill"></i>
                <p>Quản lý Tài khoản</p>
              </router-link>
            </li>

          </ul>
        </nav>
      </div>
    </aside>

    <main class="app-main">
      <div class="app-content-header py-3 shadow-sm mb-3 bg-white">
        <div class="container-fluid">
          <div class="row">
            <div class="col-sm-6">
              <h4 class="mb-0 text-primary">Hệ thống quản lý</h4>
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

    <div class="sidebar-overlay" @click="toggleSidebar"></div>

  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// --- STATE ---
const isKhoMenuOpen = ref(false);
const isAdmin = ref(false);
const currentUser = reactive({
  name: 'Người dùng',
  role: 'STAFF'
});

// --- LIFECYCLE ---
onMounted(() => {
  checkUserRole();
});

// --- METHODS ---
const checkUserRole = () => {
  const role = localStorage.getItem('userRole');
  const name = localStorage.getItem('hoTen');
  isAdmin.value = (role === 'ADMIN');
  if (name) currentUser.name = name;
  if (role) currentUser.role = role;
};

const logout = () => {
  if (confirm('Bạn có chắc muốn đăng xuất?')) {
      localStorage.removeItem('token');
      localStorage.removeItem('userRole');
      localStorage.removeItem('hoTen');
      localStorage.removeItem('userInfo');
      router.push('/login');
  }
};

const toggleKhoMenu = () => {
  // Nếu sidebar đang thu nhỏ mà bấm vào menu con -> Tự động mở rộng sidebar ra
  const body = document.querySelector('body');
  if (body.classList.contains('sidebar-collapse') && window.innerWidth >= 992) {
    body.classList.remove('sidebar-collapse');
  }
  isKhoMenuOpen.value = !isKhoMenuOpen.value;
};

const toggleSidebar = () => {
  const body = document.querySelector('body');
  if (window.innerWidth >= 992) {
    // Desktop: Thu nhỏ / Phóng to
    body.classList.toggle('sidebar-collapse');
    // Khi thu nhỏ thì đóng luôn menu con nếu đang mở
    if (body.classList.contains('sidebar-collapse')) {
      isKhoMenuOpen.value = false; 
    }
  } else {
    // Mobile: Đóng / Mở
    body.classList.toggle('sidebar-open');
  }
};
</script>

<style scoped>
.app-wrapper {
  min-height: 100vh;
  position: relative;
}
.sidebar-menu .nav-link.active {
    background-color: #0d6efd;
    color: white;
}
.nav-arrow {
    transition: transform 0.3s ease;
}
.menu-open .nav-arrow {
    transform: rotate(90deg);
}
.hover-opacity-100:hover {
  opacity: 1 !important;
  cursor: pointer;
}
</style>

<style>
/* --- HIỆU ỨNG CHUYỂN ĐỘNG CHUNG --- */
.app-sidebar, .app-header, .app-main, .app-footer, .brand-text, .sidebar-brand, .sidebar-menu .nav-link {
  transition: all 0.3s ease-in-out;
}

/* =========================================
   1. XỬ LÝ GIAO DIỆN ĐIỆN THOẠI (MOBILE < 992px)
   ========================================= */
@media (max-width: 991.98px) {
  
  /* --- A. KHUNG SIDEBAR (CỐ ĐỊNH 70PX) --- */
  .app-wrapper .app-sidebar {
    display: block !important;
    position: fixed !important;
    top: 0; left: 0; bottom: 0;
    
    width: 70px !important;
    min-width: 70px !important;
    max-width: 70px !important;
    flex: 0 0 70px !important;
    
    height: 100vh;
    background-color: #343a40;
    z-index: 1040 !important;
    transform: none !important; 
    margin-left: 0 !important;
    padding: 0 !important;
    box-shadow: none;
  }

  /* Đẩy nội dung chính sang phải 70px */
  .app-wrapper .app-header, 
  .app-wrapper .app-main, 
  .app-wrapper .app-footer {
    margin-left: 70px !important;
    width: calc(100% - 70px) !important;
  }

  /* --- B. XỬ LÝ HEADER SIDEBAR (LOGO & NÚT) --- */
  
  /* 1. Header Container: Căn giữa, bỏ padding thừa */
  .app-wrapper .sidebar-brand {
    justify-content: center !important;
    padding: 0 !important;
    width: 70px !important;
    height: 57px !important;
    display: flex !important;
    align-items: center !important;
  }

  /* 2. QUAN TRỌNG: Ẩn hoàn toàn thẻ chứa Logo (để không chiếm chỗ) */
  .sidebar-brand .brand-link {
    display: none !important;
  }

  /* 3. Căn giữa nút Toggle (Nút 3 gạch) */
  /* Chọn thẻ a không phải brand-link (tức là nút toggle) */
  .sidebar-brand > a:not(.brand-link) {
    display: flex !important;
    justify-content: center !important;
    align-items: center !important;
    width: 100% !important;
    height: 100% !important;
    margin: 0 !important;
    padding: 0 !important;
  }
  .sidebar-brand i {
    font-size: 1.5rem !important; /* Chỉnh lại icon to rõ */
  }

  /* --- C. XỬ LÝ MENU ITEM --- */
  /* Ẩn chữ menu và mũi tên */
  .sidebar-menu p,
  .sidebar-menu .nav-arrow,
  .nav-header {
    display: none !important;
  }

  /* Căn giữa Icon Menu */
  .sidebar-menu .nav-link {
    width: 100% !important;
    height: 50px !important;
    padding: 0 !important;
    margin: 0 !important;
    display: flex !important;
    justify-content: center !important;
    align-items: center !important;
  }
  .sidebar-menu .nav-icon {
    margin: 0 !important;
    font-size: 1.4rem !important;
  }
  
  /* Ẩn menu con */
  .nav-treeview { display: none !important; }


  /* --- D. TRẠNG THÁI MỞ RỘNG (KHI BẤM NÚT) --- */
  html body.sidebar-open .app-wrapper .app-sidebar {
    width: 260px !important;
    min-width: 260px !important;
    max-width: 260px !important;
    z-index: 9999 !important;
  }

  /* Hiện lại Logo và sắp xếp lại Header */
  html body.sidebar-open .sidebar-brand .brand-link { 
    display: flex !important; /* Hiện lại logo */
    align-items: center;
  }
  html body.sidebar-open .app-wrapper .sidebar-brand {
    justify-content: space-between !important; /* Logo trái, nút phải */
    padding: 0 1rem !important;
    width: 100% !important;
  }
  /* Thu nhỏ vùng bấm của nút toggle lại khi mở menu */
  html body.sidebar-open .sidebar-brand > a:not(.brand-link) {
    width: auto !important;
  }

  /* Hiện lại chữ menu */
  html body.sidebar-open .sidebar-menu p,
  html body.sidebar-open .sidebar-menu .nav-arrow { display: inline-block !important; }
  html body.sidebar-open .nav-header { display: block !important; }
  html body.sidebar-open .nav-treeview { display: block; }

  /* Style menu khi mở: Căn trái */
  html body.sidebar-open .sidebar-menu .nav-link {
    height: auto !important;
    text-align: left;
    padding: .75rem 1rem !important;
    justify-content: flex-start !important;
  }
  html body.sidebar-open .sidebar-menu .nav-icon {
    margin-right: .5rem !important;
  }

  /* Overlay */
  .sidebar-overlay {
    display: none;
    position: fixed;
    top: 0; left: 0; right: 0; bottom: 0;
    background-color: rgba(0,0,0,0.5);
    z-index: 9998;
    cursor: pointer;
  }
  html body.sidebar-open .sidebar-overlay {
    display: block;
    animation: fadeIn 0.3s forwards;
  }
}

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }


/* =========================================
   2. XỬ LÝ GIAO DIỆN MÁY TÍNH (DESKTOP >= 992px)
   ========================================= */
@media (min-width: 992px) {
  body.sidebar-collapse .app-sidebar {
    width: 70px !important;
    min-width: 70px !important;
    max-width: 70px !important;
    margin-left: 0 !important;
  }

  body.sidebar-collapse .app-header,
  body.sidebar-collapse .app-main,
  body.sidebar-collapse .app-footer {
    margin-left: 70px !important;
    width: calc(100% - 70px) !important;
  }

  /* Ẩn hoàn toàn Logo khi thu nhỏ Desktop luôn cho đồng bộ */
  body.sidebar-collapse .sidebar-brand .brand-link { display: none !important; }
  
  /* Căn giữa nút toggle */
  body.sidebar-collapse .sidebar-brand { 
    justify-content: center !important; 
    padding: 0 !important; 
  }
  body.sidebar-collapse .sidebar-brand > a:not(.brand-link) {
    display: flex !important;
    justify-content: center !important;
    align-items: center !important;
    width: 100% !important;
  }
  
  body.sidebar-collapse .sidebar-menu p,
  body.sidebar-collapse .sidebar-menu .nav-arrow,
  body.sidebar-collapse .nav-header { display: none !important; }
  
  body.sidebar-collapse .sidebar-menu .nav-link { 
    text-align: center; padding: 12px 0 !important; width: 100%;
    display: flex; justify-content: center; align-items: center; 
  }
  body.sidebar-collapse .sidebar-menu .nav-icon { margin: 0 !important; font-size: 1.4rem; }
  body.sidebar-collapse .nav-treeview { display: none !important; }
}
</style>