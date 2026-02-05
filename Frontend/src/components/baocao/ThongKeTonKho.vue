<template>
  <div class="content-wrapper">
    <div class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1 class="m-0">Báo Cáo Xuất Nhập Tồn</h1>
          </div>
        </div>
      </div>
    </div>

    <section class="content">
      <div class="container-fluid">

        <div class="card card-primary card-outline">
          <div class="card-header">
            <h3 class="card-title"><i class="fas fa-filter"></i> Bộ lọc báo cáo</h3>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-lg-3 col-md-6 col-12">
                <div class="form-group">
                  <label>Từ ngày</label>
                  <input type="date" class="form-control" v-model="filters.startDate">
                </div>
              </div>
              <div class="col-lg-3 col-md-6 col-12">
                <div class="form-group">
                  <label>Đến ngày</label>
                  <input type="date" class="form-control" v-model="filters.endDate">
                </div>
              </div>
              <div class="col-lg-3 col-md-6 col-6">
                <div class="form-group">
                  <label>Kho/Chi nhánh</label>
                  <select class="form-control" v-model="filters.warehouseId">
                    <option :value="0">Tất cả kho</option>
                    <option v-for="k in khoList" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
                  </select>
                </div>
              </div>

              <div class="col-lg-3 col-md-6 col-6">
                <div class="form-group">
                  <label>Trạng thái máy</label>
                  <select class="form-control" v-model="filters.statusId">
                    <option :value="0">Tất cả</option>
                    <option v-for="st in statusList" :key="st.id" :value="st.id">{{ st.name }}</option>
                  </select>
                </div>
              </div>
            </div>
          </div>
          <div class="card-footer">
            <button type="button" class="btn btn-primary" @click="fetchInventoryReport" :disabled="loading">
              <i class="fas" :class="loading ? 'fa-spinner fa-spin' : 'fa-search'"></i>
              {{ loading ? 'Đang tải...' : 'Xem Báo Cáo' }}
            </button>

            <button v-if="reportData.length > 0" type="button" class="btn btn-success ml-2" @click="printToWord"
              :disabled="isExporting"> <i class="fas" :class="isExporting ? 'fa-spinner fa-spin' : 'fa-file-word'"></i>
              {{ isExporting ? 'Đang xuất...' : 'Xuất Word' }}
            </button>
          </div>
        </div>

        <div class="card mt-3">
          <div class="card-header border-0">
            <h3 class="card-title text-success font-weight-bold">
              <i class="fas fa-chart-bar"></i> {{ reportTitle }}
            </h3>
            <div class="card-tools">
              <span class="badge badge-success" v-if="reportData.length > 0">{{ reportData.length }} dòng</span>
            </div>
          </div>

          <div class="card-body table-responsive p-0">
            <table class="table table-bordered table-striped table-hover text-nowrap">
              <thead class="bg-light">
                <tr>
                  <th class="text-center" width="50">STT</th>
                  <th>Mã SP</th>
                  <th>Tên Sản Phẩm </th>
                  <th class="text-center">ĐVT</th>
                  <th class="text-right">Tồn Đầu</th>
                  <th class="text-right">Nhập</th>
                  <th class="text-right">Xuất</th>
                  <th class="text-right">Tồn Cuối</th>
                  <th class="text-right">Giá BQ</th>
                  <th class="text-right">Thành Tiền</th>
                </tr>
              </thead>
              <tbody>
                <template v-if="loading">
                  <tr v-for="n in 5" :key="`skel-${n}`">
                    <td v-for="m in 10" :key="m">
                      <div class="skeleton-loader"></div>
                    </td>
                  </tr>
                </template>

                <template v-else>
                  <tr v-if="reportData.length === 0">
                    <td colspan="10" class="text-center py-4 text-muted">
                      <i class="fas fa-inbox fa-2x mb-2"></i><br>
                      Không có dữ liệu phát sinh trong khoảng thời gian này.
                    </td>
                  </tr>

                  <tr v-for="(item, index) in reportData" :key="index">
                    <td class="text-center">
                      {{ (pagination.page * pagination.size) + index + 1 }}
                    </td>
                    <td class="text-primary font-weight-bold font-monospace">{{ item.maSP }}</td>
                    <td>{{ item.tenSP }}</td>
                    <td class="text-center">{{ item.donvitinh }}</td>

                    <td class="text-right">{{ item.tonDau }}</td>
                    <td class="text-right text-success">+{{ item.nhapTrong }}</td>
                    <td class="text-right text-danger">-{{ item.xuatTrong }}</td>

                    <td class="text-right font-weight-bold bg-warning bg-opacity-25">{{ item.tonCuoi }}</td>

                    <td class="text-right">{{ formatCurrency(item.giaBQ) }}</td>
                    <td class="text-right font-weight-bold text-primary">{{ formatCurrency(item.thanhTien) }}</td>
                  </tr>

                  <tr v-if="reportData.length > 0" class="bg-secondary font-weight-bold">
                    <td colspan="4" class="text-center">TỔNG CỘNG</td>
                    <td class="text-right">{{ grandTotal.tdk }}</td>
                    <td class="text-right">{{ grandTotal.ntk }}</td>
                    <td class="text-right">{{ grandTotal.xtk }}</td>
                    <td class="text-right">{{ grandTotal.tck }}</td>
                    <td class="text-right">---</td>
                    <td class="text-right">{{ formatCurrency(grandTotal.tien) }}</td>
                  </tr>
                </template>
              </tbody>
            </table>
          </div>
          <div class="d-flex justify-content-center mt-3 px-3 pb-3" v-if="pagination.total > 0">
            <ul class="pagination pagination-sm m-0">
              <li class="page-item" :class="{ disabled: pagination.page === 0 }">
                <a class="page-link" href="#" @click.prevent="changePage(pagination.page - 1)">« Trước</a>
              </li>
              <li v-for="(page, index) in visiblePages" :key="index" class="page-item"
                :class="{ active: page === pagination.page + 1, disabled: page === '...' }">

                <a class="page-link" href="#" @click.prevent="page !== '...' ? changePage(page - 1) : null">
                  {{ page }}
                </a>
              </li>

              <li class="page-item" :class="{ disabled: pagination.page >= pagination.totalPages - 1 }">
                <a class="page-link" href="#" @click.prevent="changePage(pagination.page + 1)">Sau »</a>
              </li>
            </ul>
          </div>
        </div>

      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import api from '@/utils/axios';
import { saveAs } from "file-saver";
import PizZip from "pizzip";
import Docxtemplater from "docxtemplater";

// --- CẤU HÌNH API ---
// Sửa đường dẫn thành tương đối (vì base URL đã có /api)
const API_URL = '/thong-ke/xuat-nhap-ton';

// --- STATE ---
const today = new Date();
const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
const isExporting = ref(false);

// ---PHÂN TRANG ---
const pagination = reactive({
  page: 0,
  size: 20, // Mặc định 20 dòng/trang
  total: 0,
  totalPages: 0
});

// --- LOGIC TÍNH TOÁN HIỂN THỊ SỐ TRANG (1 ... 4 5 6 ... 10) ---
const visiblePages = computed(() => {
  const total = pagination.totalPages;
  const current = pagination.page + 1;
  const delta = 2;
  const range = [];
  const rangeWithDots = [];
  let l;

  for (let i = 1; i <= total; i++) {
    if (i === 1 || i === total || (i >= current - delta && i <= current + delta)) {
      range.push(i);
    }
  }

  for (let i of range) {
    if (l) {
      if (i - l === 2) rangeWithDots.push(l + 1);
      else if (i - l !== 1) rangeWithDots.push('...');
    }
    rangeWithDots.push(i);
    l = i;
  }
  return rangeWithDots;
});

//  HÀM CHUYỂN TRANG 
const changePage = (newPage) => {
  if (newPage < 0 || newPage >= pagination.totalPages) return;
  pagination.page = newPage;
  fetchInventoryReport();
};

const filters = reactive({
  startDate: firstDay.toISOString().substring(0, 10),
  endDate: today.toISOString().substring(0, 10),
  warehouseId: 0,
  statusId: 0
});

const khoList = ref([]);
const reportData = ref([]);
const loading = ref(false);
const currentTenKho = ref('Tất cả các kho');

const statusList = ref([
  { id: 1, name: 'Mới (New)' },
  { id: 2, name: 'Like New' },
  { id: 3, name: 'Hỏng' },
  { id: 4, name: 'Xác' },
  { id: 5, name: 'Thu hồi' },
  { id: 6, name: 'Nhập Khẩu' }
]);

const reportTitle = computed(() => {
  return `Báo cáo: ${currentTenKho.value} (${formatDateString(filters.startDate)} - ${formatDateString(filters.endDate)})`;
});


const grandTotal = ref({ tdk: 0, ntk: 0, xtk: 0, tck: 0, tien: 0 });

// --- LOAD DATA ---
const loadKho = async () => {
  try {
    // Sử dụng api.get thay vì axios.get
    const res = await api.get('/kho');
    khoList.value = res.data;
  } catch (e) {
    console.error("Lỗi tải kho:", e);
  }
};

// --- SỬA LẠI HÀM FETCH REPORT ---
const fetchInventoryReport = async () => {
  // 1. Kiểm tra ngày hợp lệ
  if (!filters.startDate || !filters.endDate) {
    alert("Vui lòng chọn đầy đủ Từ ngày và Đến ngày");
    return;
  }

  const start = new Date(filters.startDate);
  const end = new Date(filters.endDate);

  if (start > end) {
    alert("Ngày bắt đầu không được lớn hơn ngày kết thúc");
    return;
  }

  loading.value = true;
  reportData.value = [];

  try {
    // ============================================================
    // [MỚI] BƯỚC VALIDATE: KIỂM TRA CHỐT SỔ ĐẦU NĂM
    // ============================================================
    const yearToCheck = start.getFullYear(); // Lấy năm của ngày bắt đầu (Ví dụ: 2026)
    
    // Gọi API kiểm tra
    const checkRes = await api.get('/thong-ke/check-chot-so', {
        params: {
            nam: yearToCheck,
            maKho: filters.warehouseId
        }
    });

    const isChotSo = checkRes.data; // Backend trả về true/false

    // Nếu chưa chốt sổ -> Báo lỗi và Dừng lại ngay
    if (!isChotSo) {
        alert(`CẢNH BÁO: Dữ liệu năm ${yearToCheck} chưa được chốt sổ đầu năm!\n\nHệ thống không thể tính toán Tồn Đầu Kỳ chính xác.\nVui lòng vào menu "Quản Lý Chốt Tồn Kho" để chốt sổ cho năm ${yearToCheck - 1} trước.`);
        loading.value = false; // Tắt loading
        return; // <--- DỪNG HÀM TẠI ĐÂY
    }
    // ============================================================

    // 2. Nếu đã chốt rồi thì chạy tiếp logic lấy báo cáo như cũ
    const response = await api.get(API_URL, {
      params: {
        maKho: filters.warehouseId,
        tuNgay: filters.startDate,
        denNgay: filters.endDate,
        loaiLoc: filters.statusId,
        page: pagination.page,
        size: pagination.size
      }
    });

    const data = response.data;
    currentTenKho.value = data.tenKho;
    reportData.value = data.danhSachChiTiet || [];
    
    if (data.grandTotal) {
      grandTotal.value = data.grandTotal;
    }
    
    pagination.page = data.currentPage;
    pagination.total = data.totalItems;
    pagination.totalPages = data.totalPages;

  } catch (error) {
    console.error("Lỗi:", error);
    // Xử lý lỗi hiển thị đẹp hơn nếu muốn
    if (error.response && error.response.status === 400) {
        alert("Lỗi dữ liệu: " + error.response.data);
    } else {
        alert("Có lỗi xảy ra khi tải báo cáo.");
    }
  } finally {
    loading.value = false;
  }
};

// --- EXPORT WORD ---
const loadFile = async (url) => {
  const response = await fetch(url);
  if (!response.ok) throw new Error(`Lỗi tải file mẫu: ${url}`);
  return await response.arrayBuffer();
};

const printToWord = async () => {

  if (isExporting.value) return;

  isExporting.value = true;


  try {

    const response = await api.get(API_URL, {
      params: {
        maKho: filters.warehouseId,
        tuNgay: filters.startDate,
        denNgay: filters.endDate,
        loaiLoc: filters.statusId,
        page: 0,          // Luôn lấy từ trang đầu
        size: 999999      // Lấy số lượng cực lớn để bao gồm tất cả
      }
    });

    const dataToExport = response.data.danhSachChiTiet || [];
    if (dataToExport.length === 0) {
      alert("Không có dữ liệu để xuất file.");
      return;
    }

    const exportTotals = dataToExport.reduce((acc, item) => {
      acc.tdk += item.tonDau || 0;
      acc.ntk += item.nhapTrong || 0;
      acc.xtk += item.xuatTrong || 0;
      acc.tck += item.tonCuoi || 0;
      acc.tien += item.thanhTien || 0;
      return acc;
    }, { tdk: 0, ntk: 0, xtk: 0, tck: 0, tien: 0 });



    const content = await loadFile("/File_Mau_BaoCaoTonKho.docx");
    const zip = new PizZip(content);
    const doc = new Docxtemplater(zip, { paragraphLoop: true, linebreaks: true });

    const now = new Date();
    const dd = String(now.getDate()).padStart(2, '0');
    const mm = String(now.getMonth() + 1).padStart(2, '0');
    const yyyy = now.getFullYear();
    let hours = now.getHours();
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const ampm = hours >= 12 ? 'PM' : 'AM';
    hours = hours % 12; hours = hours ? hours : 12;

    const dataToRender = {
      ngayBatDau: formatDateString(filters.startDate),
      ngayKetThuc: formatDateString(filters.endDate),
      tenKho: currentTenKho.value,

      // Dùng dataToExport (Tất cả) thay vì reportData (Trang hiện tại)
      p: dataToExport.map((item, index) => ({
        stt: index + 1, // Đánh lại số thứ tự từ 1 đến hết
        ma: item.maSP,
        ten: item.tenSP,
        dvt: item.donvitinh,
        tdk: item.tonDau,
        ntk: item.nhapTrong,
        xtk: item.xuatTrong,
        tck: item.tonCuoi,
        gia: formatCurrency(item.giaBQ),
        tien: formatCurrency(item.thanhTien)
      })),

      sumTDK: exportTotals.tdk,
      sumNTK: exportTotals.ntk,
      sumXTK: exportTotals.xtk,
      sumTCK: exportTotals.tck,
      sumTien: formatCurrency(exportTotals.tien),

      d: dd, m: mm, y: yyyy, h: String(hours).padStart(2, '0'), ph: minutes, ampm: ampm
    };

    doc.render(dataToRender);
    const out = doc.getZip().generate({ type: "blob", mimeType: "application/vnd.openxmlformats-officedocument.wordprocessingml.document" });

    saveAs(out, `BaoCao_XuatNhapTon_${currentTenKho.value}_${filters.endDate}.docx`);

  } catch (error) {
    console.error(error);
    alert("Lỗi xuất file Word: " + error.message);
  } finally {
    isExporting.value = false;
  }
};

const formatCurrency = (value) => {
  if (!value || isNaN(value)) return '0';
  return new Intl.NumberFormat('vi-VN', { style: 'decimal', decimal: 'VND' }).format(value);
};

const formatDateString = (dateStr) => {
  if (!dateStr) return "...";
  const parts = dateStr.split('-');
  if (parts.length !== 3) return dateStr;
  return `${parts[2]}/${parts[1]}/${parts[0]}`;
}

onMounted(async () => {
  const token = localStorage.getItem('token'); // Hoặc tên key bạn dùng lưu token

  if (!token) {
    console.warn("Chưa có token, chuyển hướng về login...");
    // router.push('/login'); // Nếu dùng vue-router
    return;
  }


  await loadKho();
  fetchInventoryReport();


});
</script>

<style scoped>
/* --- HIỆU ỨNG SKELETON (Giữ nguyên của bạn) --- */
.skeleton-loader {
  width: 100%;
  height: 1.2em;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }

  100% {
    background-position: -200% 0;
  }
}

/* --- CSS TỐI ƯU CHO MOBILE & IPAD (Không sửa HTML) --- */

/* 1. Card Table Responsive */
/* Giới hạn chiều cao bảng để scroll dọc được trên đt, tránh bảng dài vô tận */
.card-body.table-responsive {
  max-height: 75vh;
  /* Chiếm 75% chiều cao màn hình */
  overflow-y: auto;
  border-bottom: 1px solid #dee2e6;
  -webkit-overflow-scrolling: touch;
  /* Cuộn mượt trên iPhone */
}

/* 2. Sticky Header (Dính tiêu đề khi cuộn dọc) */
.table thead th {
  position: sticky;
  top: 0;
  background-color: #f4f6f9;
  /* Màu nền trùng bg-light để che nội dung khi cuộn */
  z-index: 10;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  /* Bóng nhẹ để tách biệt */
}

/* 3. Tối ưu Bộ Lọc trên màn hình nhỏ (Mobile < 768px) */
@media (max-width: 768px) {

  /* Ép các cột col-12 thành 2 cột (50%) để 2 ô ngày tháng nằm ngang nhau */
  /* Chỉ tác động vào div bên trong card-body để an toàn */
  .card-body .row>.col-12 {
    flex: 0 0 50%;
    max-width: 50%;
  }

  /* Thu nhỏ chữ trong ô input để vừa vặn */
  .form-control {
    font-size: 13px;
    padding: 0.25rem 0.5rem;
    height: calc(1.8125rem + 2px);
    /* Chiều cao compact */
  }

  /* Label nhỏ lại */
  .form-group label {
    font-size: 12px;
    margin-bottom: 2px;
  }

  /* Nút bấm full màn hình cho dễ ấn */
  .card-footer {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .card-footer .btn {
    width: 100%;
    margin-left: 0 !important;
    /* Bỏ margin left của nút thứ 2 */
  }

  /* Font bảng nhỏ lại xíu để hiển thị nhiều dữ liệu hơn */
  .table {
    font-size: 13px;
  }

  .table th,
  .table td {
    padding: 8px 6px;
    /* Giảm padding */
  }

  /* Ẩn bớt phân trang rườm rà, chỉ giữ lại nút Trước/Sau nếu cần (CSS mặc định bootstrap tự lo việc wrap) */
  .pagination .page-link {
    padding: 0.25rem 0.5rem;
    font-size: 12px;
  }
}

/* 4. Tối ưu cho iPad / Tablet (768px - 1024px) */
@media (min-width: 768px) and (max-width: 1024px) {
  .table {
    font-size: 13px;
    /* Font vừa phải */
  }
}
</style>