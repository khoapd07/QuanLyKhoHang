<template>
  <div class="content-wrapper">
    <!-- Content Header (Page header) -->
    <div class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1 class="m-0">Báo Cáo Xuất Nhập Tồn</h1>
          </div>
          <div class="col-sm-6">
          </div>
        </div>
      </div>
    </div>
    <!-- /.content-header -->

    <!-- Main content -->
    <section class="content">
      <div class="container-fluid">
        <!-- Filter Card -->
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
                    <option value="0">Tất cả kho</option>
                    <option v-for="kho in khoList" :key="kho.id" :value="kho.id">{{ kho.name }}</option>
                  </select>
                </div>
              </div>
            </div>
          </div>
          <div class="card-footer">
            <button type="button" class="btn btn-primary" @click="fetchInventoryReport">
              <i class="fas fa-search"></i> Lọc báo cáo
            </button>
            <button type="button" class="btn btn-info mr-2" @click="printToWord">
        <i class="fas fa-file-word"></i> In Báo Cáo
      </button>
          </div>
        </div>
        <!-- /.card -->

        <!-- Report Results Card -->
        <div class="card mt-4">
          <div class="card-header">
            <h3 class="card-title">{{ reportTitle }}</h3>
          </div>
          <!-- /.card-header -->
          <div class="card-body table-responsive">
            <table class="table table-bordered table-striped table-responsive-stack">
              <thead>
                <tr>
                  <th style="width: 10px">Stt</th>
                  <th>Mã Sản Phẩm</th>
                  <th>Tên Sản Phẩm</th>
                  <th>ĐVT</th>
                  <th>TĐK</th>
                  <th>NTK</th>
                  <th>XTK</th>
                  <th>TCK</th>
                  <th>Giá/BQ</th>
                  <th>Thành Tiền</th>
                </tr>
              </thead>
              <tbody>
                <!-- Skeleton Loader -->
                <template v-if="loading">
                  <tr v-for="n in 5" :key="`skel-${n}`">
                    <td v-for="m in 10" :key="m"><div class="skeleton-loader"></div></td>
                  </tr>
                </template>

                <!-- Data Rows -->
                <template v-else>
                  <tr v-if="!reportData || reportData.length === 0">
                    <td colspan="10" class="text-center">Không có dữ liệu.</td>
                  </tr>
                  <tr v-for="(item, index) in reportData" :key="index">
                    <td>{{ index + 1 }}</td>
                    <td>{{ item.maSP }}</td>
                    <td>{{ item.tenSP }}</td>
                    <td>{{ item.donvitinh }}</td>
                    
                    <td class="text-right">{{ item.tonDau }}</td>
                    <td class="text-right">{{ item.nhapTrong }}</td>
                    <td class="text-right">{{ item.xuatTrong }}</td>
                    
                    <td class="text-right"><strong>{{ item.tonCuoi }}</strong></td>
                    
                    <td class="text-right">{{ item.giaBQ }}</td>
                    <td class="text-right"><strong>{{ item.thanhTien }}</strong></td>
                  </tr>
                </template>
              </tbody>
            </table>
          </div>
          <!-- /.card-body -->
        </div>
        <!-- /.card -->

      </div>
    </section>
    <!-- /.content -->
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import axios from 'axios';
import { saveAs } from "file-saver";
import PizZip from "pizzip";
import Docxtemplater from "docxtemplater";

// --- CẤU HÌNH ---
const API_URL = 'http://localhost:8080/api/thong-ke/xuat-nhap-ton';

// --- STATE MANAGEMENT ---
const filters = reactive({
  startDate: new Date('2024-01-01').toISOString().substring(0, 10),
  endDate: new Date().toISOString().substring(0, 10),
  warehouseId: 0,
});

const khoList = ref([
    { id: 1, name: 'Kho Tổng Hà Nội' },
    { id: 2, name: 'Kho Chi Nhánh HCM' }
]);

const reportData = ref([]);
const loading = ref(false);
const currentTenKho = ref('');

// --- FETCH API ---
const fetchInventoryReport = async () => {
  loading.value = true;
  reportData.value = [];
  try {
    const response = await axios.get(API_URL, {
      params: {
        maKho: filters.warehouseId,
        tuNgay: filters.startDate,
        denNgay: filters.endDate,
        loaiLoc: 0
      }
    });
    const data = response.data;
    currentTenKho.value = data.tenKho;
    reportData.value = data.danhSachChiTiet;
  } catch (error) {
    console.error("Lỗi:", error);
    alert("Lỗi tải dữ liệu!");
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

// --- CORE EXPORT FUNCTION ---
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

      // --- TÍNH TỔNG CỘNG (Thêm đoạn này để khớp với {sum...} trong ảnh) ---
      const totals = reportData.value.reduce((acc, item) => {
          acc.tdk += item.tonDau || 0;
          acc.ntk += item.nhapTrong || 0;
          acc.xtk += item.xuatTrong || 0;
          acc.tck += item.tonCuoi || 0;
          acc.tien += item.thanhTien || 0;
          return acc;
      }, { tdk: 0, ntk: 0, xtk: 0, tck: 0, tien: 0 });

      // --- XỬ LÝ NGÀY GIỜ AM/PM (Thêm đoạn này để khớp với {h}:{ph} {ampm}) ---
      const now = new Date();
      // Ngày tháng
      const dd = String(now.getDate()).padStart(2, '0');
      const mm = String(now.getMonth() + 1).padStart(2, '0');
      const yyyy = now.getFullYear();
      
      // Giờ phút AM/PM
      let hours = now.getHours();
      const minutes = String(now.getMinutes()).padStart(2, '0');
      const ampm = hours >= 12 ? 'PM' : 'AM';
      hours = hours % 12;
      hours = hours ? hours : 12; // Giờ 0 đổi thành 12
      const strHours = String(hours).padStart(2, '0');

      // 3. Map dữ liệu
      const dataToRender = {
          // Thông tin chung
          ngayBatDau: formatDateString(filters.startDate),
          ngayKetThuc: formatDateString(filters.endDate),
          tenKho: currentTenKho.value || "Tất cả các kho",
          
          // Dữ liệu bảng
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

          // Các biến Tổng Cộng (Khớp với hình ảnh image_376df5.png)
          sumTDK: new Intl.NumberFormat('vi-VN').format(totals.tdk),
          sumNTK: new Intl.NumberFormat('vi-VN').format(totals.ntk),
          sumXTK: new Intl.NumberFormat('vi-VN').format(totals.xtk),
          sumTCK: new Intl.NumberFormat('vi-VN').format(totals.tck),
          sumTien: formatCurrency(totals.tien),

          // Các biến Thời Gian in (Khớp với hình ảnh image_376df5.png)
          d: dd,
          m: mm,
          y: yyyy,
          h: strHours,
          ph: minutes,
          ampm: ampm
      };

      // 4. Render
      doc.render(dataToRender);

      // 5. Xuất file
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
          alert("Lỗi: " + error.message);
      }
  }
};

onMounted(() => fetchInventoryReport());
</script>

<style scoped>
/* Thêm một chút khoảng cách cho các nút trong card footer */
.card-footer .btn {
  margin-right: 5px;
}

/* Skeleton Loader CSS */
.skeleton-loader {
  width: 100%;
  height: 1.2em; /* Chiều cao tương đương dòng chữ */
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

/* Responsive Table CSS for Mobile */
@media screen and (max-width: 768px) {
  .table-responsive-stack thead {
    display: none;
  }
  .table-responsive-stack tr {
    display: block;
    margin-bottom: 1rem;
    border: 1px solid #dee2e6;
  }
  .table-responsive-stack td {
    display: block;
    text-align: right;
    border: none;
    border-bottom: 1px solid #dee2e6;
  }
  .table-responsive-stack td::before {
    content: attr(data-label);
    float: left;
    font-weight: bold;
    text-transform: uppercase;
  }
  .table-responsive-stack td:last-child {
    border-bottom: 0;
  }
  
}

</style>
