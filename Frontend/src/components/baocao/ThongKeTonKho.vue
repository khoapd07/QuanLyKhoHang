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
            <h3 class="card-title">
              <i class="fas fa-filter"></i>
              Bộ lọc báo cáo
            </h3>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-lg-4 col-md-6 col-12">
                <div class="form-group">
                  <label for="startDate">Từ ngày</label>
                  <input type="date" class="form-control" id="startDate" v-model="filters.startDate">
                </div>
              </div>
              <div class="col-lg-4 col-md-6 col-12">
                <div class="form-group">
                  <label for="endDate">Đến ngày</label>
                  <input type="date" class="form-control" id="endDate" v-model="filters.endDate">
                </div>
              </div>
              <div class="col-lg-4 col-md-6 col-12">
                <div class="form-group">
                  <label for="warehouse">Kho/Chi nhánh</label>
                  <select class="form-control" id="warehouse" v-model="filters.warehouseId">
                    <option :value="0">Tất cả kho</option>
                    <option v-for="kho in khoList" :key="kho.maKho" :value="kho.maKho">{{ kho.tenKho }}</option>
                  </select>
                </div>
              </div>
            </div>
          </div>
          <div class="card-footer">
            <button type="button" class="btn btn-primary" @click="fetchInventoryReport">
              <i class="fas fa-search"></i> Lọc báo cáo
            </button>
            <button v-if="reportData.length > 0" type="button" class="btn btn-info mr-2 ml-2" @click="printToWord">
              <i class="fas fa-file-word"></i> In Báo Cáo
            </button>
          </div>
        </div>

        <div class="card mt-4">
          <div class="card-header">
            <h3 class="card-title text-success"><i class="fas fa-chart-line"></i> {{ reportTitle }}</h3>
          </div>
          
          <div class="card-body table-responsive p-0">
            <table class="table table-bordered table-striped table-hover">
              <thead class="bg-light">
                <tr>
                  <th style="width: 50px" class="text-center">STT</th>
                  <th>Mã SP</th>
                  <th>Tên Sản Phẩm</th>
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
                  <tr v-if="!reportData || reportData.length === 0">
                    <td colspan="10" class="text-center py-4 text-muted">
                        <i class="fas fa-inbox fa-2x mb-2"></i><br>
                        Không có dữ liệu trong khoảng thời gian này.
                    </td>
                  </tr>
                  <tr v-for="(item, index) in reportData" :key="index">
                    <td class="text-center">{{ index + 1 }}</td>
                    <td class="font-monospace text-primary">{{ item.maSP }}</td>
                    <td class="fw-bold">{{ item.tenSP }}</td>
                    <td class="text-center">{{ item.donvitinh }}</td>
                    
                    <td class="text-right">{{ item.tonDau }}</td>
                    <td class="text-right text-success">+{{ item.nhapTrong }}</td>
                    <td class="text-right text-danger">-{{ item.xuatTrong }}</td>
                    
                    <td class="text-right bg-warning bg-opacity-10 fw-bold">{{ item.tonCuoi }}</td>
                    
                    <td class="text-right">{{ formatCurrency(item.giaBQ) }}</td>
                    <td class="text-right fw-bold text-primary">{{ formatCurrency(item.thanhTien) }}</td>
                  </tr>
                </template>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
// [QUAN TRỌNG] Dùng api từ utils
import api from '@/utils/axios';
import { saveAs } from "file-saver";
import PizZip from "pizzip";
import Docxtemplater from "docxtemplater";

// --- CẤU HÌNH API ---
// URL: /api/thong-ke/xuat-nhap-ton (Khớp Controller)
const API_URL = '/thong-ke/xuat-nhap-ton';

// --- STATE ---
const filters = reactive({
  startDate: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10), // Đầu tháng hiện tại
  endDate: new Date().toISOString().substring(0, 10), // Hôm nay
  warehouseId: 0,
});

const khoList = ref([]);
const reportData = ref([]);
const loading = ref(false);
const currentTenKho = ref('Tất cả các kho');

// Tiêu đề báo cáo động
const reportTitle = computed(() => {
    return `Báo cáo: ${currentTenKho.value} (${formatDateString(filters.startDate)} - ${formatDateString(filters.endDate)})`;
});

// --- LOAD DATA ---
const loadKho = async () => {
    try {
        // API: /api/kho (Lấy danh sách kho)
        const res = await api.get('/kho');
        khoList.value = res.data;
    } catch (e) {
        console.error("Lỗi tải kho:", e);
    }
};

const fetchInventoryReport = async () => {
  loading.value = true;
  reportData.value = [];
  try {
    const response = await api.get(API_URL, {
      params: {
        maKho: filters.warehouseId,
        tuNgay: filters.startDate,
        denNgay: filters.endDate,
        loaiLoc: 0 // 0: Tất cả, 1: Chỉ có phát sinh... (Tùy logic backend)
      }
    });
    
    const data = response.data;
    // Backend trả về DTO: { tenKho: "...", danhSachChiTiet: [...] }
    currentTenKho.value = data.tenKho;
    reportData.value = data.danhSachChiTiet;
    
  } catch (error) {
    console.error("Lỗi:", error);
    const msg = error.response?.data?.message || error.message;
    alert("Lỗi tải báo cáo: " + msg);
  } finally {
    loading.value = false;
  }
};

// --- HELPER FUNCTIONS ---

const loadFile = async (url) => {
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error(`Không thể tải file mẫu: ${url}`);
    }
    return await response.arrayBuffer();
};

const formatCurrency = (value) => {
  if (!value || isNaN(value)) return '0';
  return new Intl.NumberFormat('vi-VN').format(value);
};

const formatDateString = (dateStr) => {
    if(!dateStr) return "...";
    const parts = dateStr.split('-');
    if(parts.length !== 3) return dateStr;
    return `${parts[2]}/${parts[1]}/${parts[0]}`;
}

// --- EXPORT WORD ---
const printToWord = async () => {
  if (!reportData.value || reportData.value.length === 0) {
    alert("Không có dữ liệu để in.");
    return;
  }

  try {
      // 1. Tải file mẫu
      const content = await loadFile("/File_Mau_BaoCaoTonKho.docx");

      // 2. Khởi tạo Docxtemplater
      const zip = new PizZip(content);
      const doc = new Docxtemplater(zip, {
          paragraphLoop: true,
          linebreaks: true,
      });

      // --- TÍNH TỔNG ---
      const totals = reportData.value.reduce((acc, item) => {
          acc.tdk += item.tonDau || 0;
          acc.ntk += item.nhapTrong || 0;
          acc.xtk += item.xuatTrong || 0;
          acc.tck += item.tonCuoi || 0;
          acc.tien += item.thanhTien || 0; // Backend trả về BigDecimal -> JSON là number
          return acc;
      }, { tdk: 0, ntk: 0, xtk: 0, tck: 0, tien: 0 });

      // --- NGÀY GIỜ ---
      const now = new Date();
      const dd = String(now.getDate()).padStart(2, '0');
      const mm = String(now.getMonth() + 1).padStart(2, '0');
      const yyyy = now.getFullYear();
      
      let hours = now.getHours();
      const minutes = String(now.getMinutes()).padStart(2, '0');
      const ampm = hours >= 12 ? 'PM' : 'AM';
      hours = hours % 12;
      hours = hours ? hours : 12; 
      const strHours = String(hours).padStart(2, '0');

      // 3. Map dữ liệu
      const dataToRender = {
          // Header Info
          ngayBatDau: formatDateString(filters.startDate),
          ngayKetThuc: formatDateString(filters.endDate),
          tenKho: currentTenKho.value,
          
          // Data Table Loop
          p: reportData.value.map((item, index) => ({
              stt: index + 1,
              ma: item.maSP || "",
              ten: item.tenSP || "",
              dvt: item.donvitinh || "",
              tdk: item.tonDau || 0,
              ntk: item.nhapTrong || 0,
              xtk: item.xuatTrong || 0,
              tck: item.tonCuoi || 0,
              gia: formatCurrency(item.giaBQ),
              tien: formatCurrency(item.thanhTien)
          })),

          // Footer Sums
          sumTDK: new Intl.NumberFormat('vi-VN').format(totals.tdk),
          sumNTK: new Intl.NumberFormat('vi-VN').format(totals.ntk),
          sumXTK: new Intl.NumberFormat('vi-VN').format(totals.xtk),
          sumTCK: new Intl.NumberFormat('vi-VN').format(totals.tck),
          sumTien: formatCurrency(totals.tien),

          // Print Date
          d: dd, m: mm, y: yyyy, h: strHours, ph: minutes, ampm: ampm
      };

      doc.render(dataToRender);

      const out = doc.getZip().generate({
          type: "blob",
          mimeType: "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      });
      
      saveAs(out, `BaoCao_XNT_${filters.endDate}.docx`);

  } catch (error) {
      console.error("Lỗi in Word:", error);
      if (error.properties && error.properties.errors) {
          const errs = error.properties.errors.map(e => e.properties.explanation).join("\n");
          alert("Lỗi Template Word: \n" + errs);
      } else {
          alert("Lỗi xuất file: " + error.message);
      }
  }
};

// --- LIFECYCLE ---
onMounted(async () => {
    await loadKho();
    fetchInventoryReport();
});
</script>

<style scoped>
.skeleton-loader {
  width: 100%;
  height: 1.2em;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>