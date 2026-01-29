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
              <i class="fas fa-filter"></i> Bộ lọc báo cáo
            </h3>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-lg-3 col-md-6 col-12">
                <div class="form-group">
                  <label for="startDate">Từ ngày</label>
                  <input type="date" class="form-control" id="startDate" v-model="filters.startDate">
                </div>
              </div>
              
              <div class="col-lg-3 col-md-6 col-12">
                <div class="form-group">
                  <label for="endDate">Đến ngày</label>
                  <input type="date" class="form-control" id="endDate" v-model="filters.endDate">
                </div>
              </div>

              <div class="col-lg-3 col-md-6 col-12">
                <div class="form-group">
                  <label for="warehouse">Kho/Chi nhánh</label>
                  <select class="form-control" id="warehouse" v-model="filters.warehouseId">
                    <option :value="0"> Tất cả kho </option>
                    <option v-for="kho in khoList" :key="kho.id" :value="kho.id">{{ kho.name }}</option>
                  </select>
                </div>
              </div>

              <div class="col-lg-3 col-md-6 col-12">
                <div class="form-group">
                  <label for="status">Trạng thái</label>
                  <select class="form-control" id="status" v-model="filters.statusId" @change="fetchInventoryReport">
                    <option :value="0"> Tất cả </option>
                    <option v-for="st in statusList" :key="st.id" :value="st.id">{{ st.name }}</option>
                  </select>
                </div>
              </div>
            </div>
          </div>
          
          <div class="card-footer">
            <button type="button" class="btn btn-primary" @click="fetchInventoryReport">
              <i class="fas fa-search"></i> Lọc dữ liệu
            </button>
            <button type="button" class="btn btn-info mr-2" @click="printToWord">
              <i class="fas fa-file-word"></i> Xuất file Word
            </button>
          </div>
        </div>

        <div class="card mt-4">
          <div class="card-header border-0">
            <h3 class="card-title font-weight-bold">
                Kết quả: {{ currentTenKho || 'Chưa có dữ liệu' }}
            </h3>
            <div class="card-tools">
                <span class="badge badge-success" v-if="reportData.length > 0">{{ reportData.length }} bản ghi</span>
            </div>
          </div>
          
          <div class="card-body table-responsive p-0">
            <table class="table table-bordered table-striped table-hover">
              <thead class="bg-light">
                <tr class="text-center">
                  <th style="width: 50px">STT</th>
                  <th>Mã SP</th>
                  <th>Tên Sản Phẩm</th>
                  <th>ĐVT</th>
                  <th>Tồn Đầu</th>
                  <th>Nhập</th>
                  <th>Xuất</th>
                  <th>Tồn Cuối</th>
                  <th>Giá BQ</th>
                  <th>Thành Tiền</th>
                </tr>
              </thead>
              <tbody>
                <template v-if="loading">
                  <tr v-for="n in 5" :key="`skel-${n}`">
                    <td v-for="m in 10" :key="m"><div class="skeleton-loader"></div></td>
                  </tr>
                </template>

                <template v-else>
                  <tr v-if="!reportData || reportData.length === 0">
                    <td colspan="10" class="text-center py-4 text-muted">
                        <i class="fas fa-box-open fa-3x mb-3"></i><br>
                        Không tìm thấy dữ liệu phù hợp.
                    </td>
                  </tr>
                  
                  <tr v-for="(item, index) in reportData" :key="index">
                    <td class="text-center">{{ index + 1 }}</td>
                    <td class="font-weight-bold text-primary">{{ item.maSP }}</td>
                    <td>{{ item.tenSP }}</td>
                    <td class="text-center">{{ item.donvitinh }}</td>
                    
                    <td class="text-right">{{ item.tonDau }}</td>
                    <td class="text-right text-success">{{ item.nhapTrong }}</td>
                    <td class="text-right text-danger">{{ item.xuatTrong }}</td>
                    
                    <td class="text-right font-weight-bold" style="background-color: #f8f9fa">
                        {{ item.tonCuoi }}
                    </td>
                    
                    <td class="text-right">{{ formatCurrency(item.giaBQ) }}</td>
                    <td class="text-right font-weight-bold text-success">
                        {{ formatCurrency(item.thanhTien) }}
                    </td>
                  </tr>

                  <tr v-if="reportData.length > 0" class="bg-warning font-weight-bold">
                      <td colspan="4" class="text-center">TỔNG CỘNG</td>
                      <td class="text-right">{{ sumTotals.tdk }}</td>
                      <td class="text-right">{{ sumTotals.ntk }}</td>
                      <td class="text-right">{{ sumTotals.xtk }}</td>
                      <td class="text-right">{{ sumTotals.tck }}</td>
                      <td></td>
                      <td class="text-right">{{ formatCurrency(sumTotals.tien) }}</td>
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
import axios from 'axios';
import { saveAs } from "file-saver";
import PizZip from "pizzip";
import Docxtemplater from "docxtemplater";

// --- CẤU HÌNH ---
const API_URL = 'http://localhost:8080/api/thong-ke/xuat-nhap-ton';

// --- STATE MANAGEMENT ---
const filters = reactive({
  // Mặc định lấy từ đầu tháng hiện tại
  startDate: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10),
  endDate: new Date().toISOString().substring(0, 10),
  warehouseId: 0,
  statusId: 0, // 0 = Tất cả
});

// Danh sách trạng thái (Khớp ID với Database)
const statusList = ref([
    { id: 1, name: 'Mới' },
    { id: 2, name: 'Like New' },
    { id: 3, name: 'Hỏng' },
    { id: 4, name: 'Xác' },
    { id: 5, name: 'Thu hồi' }
]);

const khoList = ref([
    { id: 1, name: 'Kho Tổng Hà Nội' },
    { id: 2, name: 'Kho Chi Nhánh HCM' }
]);

const reportData = ref([]);
const loading = ref(false);
const currentTenKho = ref('');

// --- COMPUTED: TÍNH TỔNG ---
const sumTotals = computed(() => {
    return reportData.value.reduce((acc, item) => {
        acc.tdk += item.tonDau || 0;
        acc.ntk += item.nhapTrong || 0;
        acc.xtk += item.xuatTrong || 0;
        acc.tck += item.tonCuoi || 0;
        acc.tien += (typeof item.thanhTien === 'number' ? item.thanhTien : 0);
        return acc;
    }, { tdk: 0, ntk: 0, xtk: 0, tck: 0, tien: 0 });
});

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
        loaiLoc: filters.statusId // Truyền statusId lên để Java lọc (hoặc SQL lọc)
      }
    });
    const data = response.data;
    currentTenKho.value = data.tenKho;
    reportData.value = data.danhSachChiTiet;
  } catch (error) {
    console.error("Lỗi:", error);
    // Có thể dùng Toast/Notification thay vì alert
    alert("Không thể tải dữ liệu báo cáo!");
  } finally {
    loading.value = false;
  }
};

// --- HELPER FUNCTIONS ---
const loadFile = async (url) => {
    const response = await fetch(url);
    if (!response.ok) throw new Error(`Không thể tải file mẫu: ${url}`);
    return await response.arrayBuffer();
};

const formatCurrency = (value) => {
  if (!value || isNaN(value)) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

const formatDateString = (dateStr) => {
    if(!dateStr) return "...";
    const parts = dateStr.split('-');
    if(parts.length !== 3) return dateStr;
    return `${parts[2]}/${parts[1]}/${parts[0]}`;
}

// --- EXPORT WORD FUNCTION ---
const printToWord = async () => {
  if (!reportData.value || reportData.value.length === 0) {
    alert("Không có dữ liệu để in.");
    return;
  }

  try {
      // 1. Load Template
      const content = await loadFile("/File_Mau_BaoCaoTonKho.docx");
      const zip = new PizZip(content);
      const doc = new Docxtemplater(zip, { paragraphLoop: true, linebreaks: true });

      // 2. Chuẩn bị dữ liệu thời gian
      const now = new Date();
      const dd = String(now.getDate()).padStart(2, '0');
      const mm = String(now.getMonth() + 1).padStart(2, '0');
      const yyyy = now.getFullYear();
      let hours = now.getHours();
      const minutes = String(now.getMinutes()).padStart(2, '0');
      const ampm = hours >= 12 ? 'PM' : 'AM';
      hours = hours % 12; hours = hours ? hours : 12;

      // 3. Map dữ liệu vào template
      const dataToRender = {
          ngayBatDau: formatDateString(filters.startDate),
          ngayKetThuc: formatDateString(filters.endDate),
          tenKho: currentTenKho.value || "Tất cả các kho",
          
          // Dữ liệu bảng chi tiết
          p: reportData.value.map((item, index) => ({
              stt: index + 1,
              ma: item.maSP,
              ten: item.tenSP, // Tên đã có trạng thái từ Java (Ví dụ: IP15 (Mới))
              dvt: item.donvitinh,
              tdk: item.tonDau,
              ntk: item.nhapTrong,
              xtk: item.xuatTrong,
              tck: item.tonCuoi,
              gia: new Intl.NumberFormat('vi-VN').format(item.giaBQ),
              tien: new Intl.NumberFormat('vi-VN').format(item.thanhTien)
          })),

          // Các biến tổng cộng
          sumTDK: new Intl.NumberFormat('vi-VN').format(sumTotals.value.tdk),
          sumNTK: new Intl.NumberFormat('vi-VN').format(sumTotals.value.ntk),
          sumXTK: new Intl.NumberFormat('vi-VN').format(sumTotals.value.xtk),
          sumTCK: new Intl.NumberFormat('vi-VN').format(sumTotals.value.tck),
          sumTien: new Intl.NumberFormat('vi-VN').format(sumTotals.value.tien),

          // Ngày giờ in
          d: dd, m: mm, y: yyyy,
          h: String(hours).padStart(2,'0'), ph: minutes, ampm: ampm
      };

      // 4. Render & Save
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

// Tự động tải dữ liệu khi trang vừa mở
onMounted(() => fetchInventoryReport());
</script>

<style scoped>
/* Tùy chỉnh khoảng cách nút */
.card-footer .btn {
  margin-right: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

/* Hiệu ứng Skeleton Loader */
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

/* Căn chỉnh bảng */
.table th {
    vertical-align: middle;
}
</style>