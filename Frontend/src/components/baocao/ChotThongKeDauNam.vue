<template>
  <div class="content-wrapper">
    <div class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6"><h1 class="m-0">Chốt Tồn Kho Đầu Năm</h1></div>
        </div>
      </div>
    </div>

    <section class="content">
      <div class="container-fluid">
        <div class="card card-warning card-outline">
          <div class="card-header">
            <h3 class="card-title"><i class="fas fa-cogs"></i> Cấu hình chốt sổ</h3>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-lg-6 col-md-6 col-12">
                <div class="form-group">
                  <label>Chọn Năm Cần Chốt</label>
                  <input type="number" class="form-control" v-model="filters.nam" placeholder="Ví dụ: 2026">
                </div>
              </div>
              
              <div class="col-lg-6 col-md-6 col-12">
                <div class="form-group">
                  <label>Kho/Chi nhánh áp dụng</label>
                  <select class="form-control" v-model="filters.warehouseId">
                    <option v-for="kho in khoList" :key="kho.id" :value="kho.id">{{ kho.name }}</option>
                  </select>
                </div>
              </div>
            </div>
          </div>
          
          <div class="card-footer">
            <button type="button" class="btn btn-warning" @click="thucHienChotSo" :disabled="loading">
              <i class="fas fa-save"></i> {{ loading ? 'Đang xử lý...' : 'Thực Hiện Chốt Sổ' }}
            </button>
            
            <button v-if="reportData.length > 0" type="button" class="btn btn-info float-right" @click="printToWord">
              <i class="fas fa-file-word"></i> In Biên Bản Chốt
            </button>
          </div>
        </div>

        <div class="card mt-4" v-if="reportData.length > 0">
          <div class="card-header">
            <h3 class="card-title text-success"><i class="fas fa-check-circle"></i> {{ reportTitle }}</h3>
          </div>
          <div class="card-body table-responsive">
            <table class="table table-bordered table-striped">
              <thead>
                <tr>
                  <th style="width: 10px">Stt</th>
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
                  <tr v-for="(item, index) in reportData" :key="index">
                    <td>{{ index + 1 }}</td>
                    <td>{{ item.maSP }}</td>
                    <td>{{ item.tenSP }}</td>
                    <td>{{ item.donvitinh }}</td>
                    
                    <td class="text-right">{{ item.tonDau }}</td>
                    <td class="text-right">{{ item.nhapTrong }}</td>
                    <td class="text-right">{{ item.xuatTrong }}</td>
                    
                    <td class="text-right"><strong>{{ item.tonCuoi }}</strong></td>
                    
                    <td class="text-right">{{ formatCurrency(item.giaBQ) }}</td>
                    <td class="text-right"><strong>{{ formatCurrency(item.thanhTien) }}</strong></td>
                  </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import axios from 'axios';
import { saveAs } from "file-saver";
import PizZip from "pizzip";
import Docxtemplater from "docxtemplater";

// --- CẤU HÌNH API ---
const API_BASE = 'http://localhost:8080/api/thong-ke'; 

// --- STATE ---
const filters = reactive({
  nam: new Date().getFullYear() - 1, // Mặc định năm sau
  warehouseId: 1, 
});

const khoList = ref([
    { id: 1, name: 'Kho Tổng Hà Nội' },
    { id: 2, name: 'Kho Chi Nhánh HCM' }
]);

const reportData = ref([]);
const loading = ref(false);
const currentTenKho = ref('');

// --- HELPER FUNCTIONS ---

// 1. Hàm tải file mẫu (Dùng fetch cho hiện đại, không lỗi)
const loadFile = async (url) => {
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error(`Không thể tải file mẫu: ${url}`);
    }
    return await response.arrayBuffer();
};

// 2. Format tiền tệ
const formatCurrency = (value) => {
  if (value === null || value === undefined || isNaN(value)) return '0';
  return new Intl.NumberFormat('vi-VN').format(value);
};

// --- API: CHỐT SỔ (POST) ---
const thucHienChotSo = async () => {
  if(!confirm(`Bạn có chắc chắn muốn chốt sổ cho năm ${filters.nam} tại kho này không? Dữ liệu cũ của năm này sẽ bị ghi đè!`)) {
      return;
  }

  loading.value = true;
  reportData.value = []; // Reset bảng

  try {
    const response = await axios.post(`${API_BASE}/chot-so`, null, {
      params: {
        nam: filters.nam,
        maKho: filters.warehouseId
      }
    });

    const data = response.data;
    currentTenKho.value = data.tenKho;
    reportData.value = data.danhSachChiTiet;
    
    alert("Chốt sổ thành công!");

  } catch (error) {
    console.error("Lỗi:", error);
    alert("Lỗi khi chốt sổ: " + (error.response?.data || error.message));
  } finally {
    loading.value = false;
  }
};

// --- IN WORD (DÙNG TEMPLATE) ---
const printToWord = async () => {
  if (!reportData.value || reportData.value.length === 0) {
    alert("Không có dữ liệu để in.");
    return;
  }

  try {
      // 1. Tải file mẫu từ thư mục public
      // Bạn cần tạo file này theo hướng dẫn bên dưới
      const content = await loadFile("/File_Mau_BaoCaoChotSoNam.docx");

      // 2. Khởi tạo Docxtemplater
      const zip = new PizZip(content);
      const doc = new Docxtemplater(zip, {
          paragraphLoop: true,
          linebreaks: true,
      });

      // --- TÍNH TỔNG CỘNG ---
      const totals = reportData.value.reduce((acc, item) => {
          acc.tdk += item.tonDau || 0;
          acc.ntk += item.nhapTrong || 0;
          acc.xtk += item.xuatTrong || 0;
          acc.tck += item.tonCuoi || 0;
          acc.tien += item.thanhTien || 0;
          return acc;
      }, { tdk: 0, ntk: 0, xtk: 0, tck: 0, tien: 0 });

      // --- XỬ LÝ NGÀY GIỜ (AM/PM) ---
      const today = new Date();
      
      const dd = String(today.getDate()).padStart(2, '0');
      const mm = String(today.getMonth() + 1).padStart(2, '0');
      const yyyy = today.getFullYear();

      let hours = today.getHours();
      const minutes = String(today.getMinutes()).padStart(2, '0');
      const ampm = hours >= 12 ? 'PM' : 'AM';
      hours = hours % 12;
      hours = hours ? hours : 12; // Giờ 0 đổi thành 12
      const strHours = String(hours).padStart(2, '0');

      // 3. Map dữ liệu vào Template
      const dataToRender = {
          nam: filters.nam,
          tenKho: currentTenKho.value || "Kho chưa xác định",
          
          // Ngày giờ in
          d: dd, m: mm, y: yyyy, h: strHours, ph: minutes, ampm: ampm,

          // Các biến tổng cộng
          sumTDK: new Intl.NumberFormat('vi-VN').format(totals.tdk),
          sumNTK: new Intl.NumberFormat('vi-VN').format(totals.ntk),
          sumXTK: new Intl.NumberFormat('vi-VN').format(totals.xtk),
          sumTCK: new Intl.NumberFormat('vi-VN').format(totals.tck),
          sumTien: formatCurrency(totals.tien),

          // Vòng lặp bảng: {#p} ... {/p}
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
          }))
      };

      // 4. Render dữ liệu
      doc.render(dataToRender);

      // 5. Xuất file
      const out = doc.getZip().generate({
          type: "blob",
          mimeType: "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      });
      
      saveAs(out, `BienBanChotSo_${filters.nam}.docx`);

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
</script>

<style scoped>
/* CSS cho đẹp form */
.card-warning.card-outline {
  border-top: 3px solid #ffc107;
}
</style>